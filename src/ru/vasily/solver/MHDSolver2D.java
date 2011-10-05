package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.utils.AllInOneMHDSolver2DReporter;
import ru.vasily.solver.utils.MHDSolver2DReporter;
import ru.vasily.solver.utils.Restorator2dUtility;
import ru.vasily.solverhelper.PlotData;

import static java.lang.Math.*;
import static ru.vasily.solver.Utils.*;
import static ru.vasily.solverhelper.misc.ArrayUtils.copy;
import static ru.vasily.solverhelper.misc.ArrayUtils.isNAN;

public class MHDSolver2D implements MHDSolver
{
	private int count = 0;

	private double totalTime = 0;
	private final double[][][] predictorData;
	private final double[][][] correctorData;
	private final double[][][] left_right_flow;
	private final double[][][] up_down_flow;
	private final double[][] divB;

	@Override
	public double getTotalTime()
	{
		return totalTime;
	}

	private final int xRes;
	private final int yRes;
	private final double gamma;
	private final double hx;
	private final double hy;
	private final double CFL;
	private final Restorator2dUtility restorator;
	private final MHDSolver2DReporter reporter;

	private final RiemannSolver2D riemannSolver2d;

	public MHDSolver2D(DataObject params, ThreePointRestorator restorator,
					   RiemannSolver2D riemannSolver, double[][][] initialValues)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");
		xRes = calculationConstants.getInt("xRes");
		yRes = calculationConstants.getInt("yRes");
		hx = physicalConstants.getDouble("xLength") / (xRes - 1);
		hy = physicalConstants.getDouble("yLength") / (yRes - 1);
		gamma = physicalConstants.getDouble("gamma");
		CFL = calculationConstants.getDouble("CFL");
		riemannSolver2d = riemannSolver;
		predictorData = initialValues;
		correctorData = copy(initialValues);
		left_right_flow = new double[xRes][yRes][8];
		up_down_flow = new double[xRes][yRes][8];
		divB = new double[xRes][yRes];
		this.restorator = new Restorator2dUtility(restorator, predictorData,
				gamma);
		this.reporter = new AllInOneMHDSolver2DReporter();
	}

	@Override
	public void nextTimeStep()
	{
		double tau = getTau();
		applyBorder(predictorData);
		findPredictorFlow();
		applyFlow(tau / 2, predictorData);
		applyBorder(predictorData);

		findCorrectorFlowAndDivB();
		applyFlow(tau, correctorData);
		calculateDivB();
		applyMagneticChargeFlow(tau);
		applyBorder(correctorData);

		copy(predictorData, correctorData);
		count++;
		totalTime += tau;
	}

	private void calculateDivB()
	{
		double[] temp = new double[8];
		for (int i = 2; i < xRes - 1; i++)
		{
			for (int j = 2; j < yRes - 1; j++)
			{
				double bY_cell_top = restorator.restoreDown(temp, i, j)[6];
				double bY_cell_bottom = restorator.restoreUp(temp, i, j - 1)[6];
				double bX_cell_left_side =
						restorator.restoreRight(temp, i - 1, j)[5];
				double bX_cell_right_side =
						restorator.restoreLeft(temp, i, j)[5];
				divB[i][j] = (bY_cell_top - bY_cell_bottom) / hy +
						(bX_cell_right_side - bX_cell_left_side) / hx;
			}
		}
	}

	private void applyMagneticChargeFlow(double tau)
	{
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 1; j++)
			{
				double[] val = correctorData[i][j];
				double ro = val[0];
				double roU = val[1];
				double roV = val[2];
				double roW = val[3];
				double U = roU / ro;
				double V = roV / ro;
				double W = roW / ro;
				double bX = val[5];
				double bY = val[6];
				double bZ = val[7];
				double divB_tau = divB[i][j] * tau;
				double pi_4 = PI * 4;
				val[1] -= bX / pi_4 * divB_tau;
				val[2] -= bY / pi_4 * divB_tau;
				val[3] -= bZ / pi_4 * divB_tau;
				val[4] -= (U * bX + V * bY + W * bZ) / pi_4 * divB_tau;
				val[5] -= U * divB_tau;
				val[6] -= V / pi_4 * divB_tau;
				val[7] -= W / pi_4 * divB_tau;
			}
	}

	private void applyBorder(double[][][] data)
	{
		for (int i = 0; i < xRes; i++)
		{
			copy(data[i][1], data[i][2]);
			copy(data[i][0], data[i][1]);
			copy(data[i][yRes - 2], data[i][yRes - 3]);
			copy(data[i][yRes - 1], data[i][yRes - 2]);
		}
		for (int j = 0; j < yRes; j++)
		{
			copy(data[0][j], data[1][j]);
			copy(data[1][j], data[2][j]);
			copy(data[xRes - 2][j], data[xRes - 3][j]);
			copy(data[xRes - 1][j], data[xRes - 2][j]);
		}
	}

	private double getTau()
	{
		double tau = Double.POSITIVE_INFINITY;
		double[] u_phy = new double[8];
		double lengthCharacteristic = min(hx, hy);
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(u_phy, predictorData[i][j], gamma);
				double U = u_phy[1];
				double V = u_phy[2];
				double speedOfFlow = sqrt(U * U + V * V);
				double currentSpeed = speedOfFlow + maximumFastShockSpeed(u_phy, gamma);
				tau = min(lengthCharacteristic / currentSpeed, tau);
			}
		return tau * CFL;
	}

	private void findPredictorFlow()
	{
		double[] uLeft_phy = new double[8];
		double[] uRight_phy = new double[8];
		double[] uUp_phy = new double[8];
		double[] uDown_phy = new double[8];

		for (int i = 0; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 1; j++)
			{
				double[] uLeft = predictorData[i][j];
				toPhysical(uLeft_phy, uLeft, gamma);
				double[] uRight = predictorData[i + 1][j];
				toPhysical(uRight_phy, uRight, gamma);
				double[] flow = left_right_flow[i][j];
				setFlow(flow, uLeft_phy, uRight_phy, i, j, 1.0, 0.0);
			}
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 0; j < yRes - 1; j++)
			{
				double[] uDown = predictorData[i][j];
				toPhysical(uDown_phy, uDown, gamma);
				double[] uUp = predictorData[i][j + 1];
				toPhysical(uUp_phy, uUp, gamma);
				double[] flow = up_down_flow[i][j];
				setFlow(flow, uDown_phy, uUp_phy, i, j, 0.0, 1.0);
			}
	}

	private void findCorrectorFlowAndDivB()
	{
		double[] uLeft_phy = new double[8];
		double[] uRight_phy = new double[8];
		double[] uUp_phy = new double[8];
		double[] uDown_phy = new double[8];

		for (int i = 1; i < xRes - 2; i++)
			for (int j = 1; j < yRes - 1; j++)
			{
				double[] flow = left_right_flow[i][j];
				restorator.restoreLeft(uLeft_phy, i, j);
				restorator.restoreRight(uRight_phy, i, j);
				setFlow(flow, uLeft_phy, uRight_phy, i, j, 1.0, 0.0);
			}
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 2; j++)
			{
				double[] flow = up_down_flow[i][j];
				restorator.restoreUp(uUp_phy, i, j);
				restorator.restoreDown(uDown_phy, i, j);
				setFlow(flow, uDown_phy, uUp_phy, i, j, 0.0, 1.0);
			}
	}

	private void applyFlow(double timeStep, double[][][] consVal)
	{
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 1; j++)
				for (int k = 0; k < 8; k++)
				{
					final double up_down_diff = up_down_flow[i][j - 1][k]
							- up_down_flow[i][j][k];
					double d = up_down_diff * timeStep / hy;
					consVal[i][j][k] += d;

					final double left_right_diff = left_right_flow[i - 1][j][k]
							- left_right_flow[i][j][k];
					consVal[i][j][k] += left_right_diff
							* timeStep / hx;
				}
	}

	private void setFlow(double[] flow, double[] uL, double[] uR, int i, int j,
						 double cos_alfa, double sin_alfa)
	{
		riemannSolver2d.getFlow(flow, uL, uR, gamma, gamma, cos_alfa, sin_alfa);
		if (isNAN(flow))
		{
			throw AlgorithmError.builder()
					.put("error type", "NAN value in flow")
					.put("total time", getTotalTime()).put("count", count)
					.put("i", i).put("j", j)
					.put("left_input", uL).put("right_input", uR)
					.put("output", flow)
					.put("cos_alfa", cos_alfa).put("sin_alfa", sin_alfa)
					.build();
		}
	}

	@Override
	public ImmutableMap<String, Object> getLogData()
	{
		return ImmutableMap.<String, Object>builder()
				.put("count", count)
				.put("total time", totalTime)
				.put("1_1_up_down_flow", up_down_flow[1][1])
				.put("1_0_up_down_flow", up_down_flow[1][0])
				.put("1_1_left_right_flow", left_right_flow[1][1])
				.put("0_1_left_right_flow", left_right_flow[0][1])
				.build();
	}

	@Override
	public PlotData getData()
	{
		return reporter.report(xCoordinates(), yCoordinates(), predictorData, divB, gamma);
	}

	private double[][] xCoordinates()
	{
		double[][] x = new double[xRes][yRes];
		for (int i = 0; i < xRes; i++)
		{
			for (int j = 0; j < yRes; j++)
			{
				x[i][j] = i * hx;
			}
		}
		return x;
	}

	private double[][] yCoordinates()
	{
		double[][] y = new double[xRes][yRes];
		for (int i = 0; i < xRes; i++)
		{
			for (int j = 0; j < yRes; j++)
			{
				y[i][j] = j * hy;
			}
		}
		return y;
	}

}
