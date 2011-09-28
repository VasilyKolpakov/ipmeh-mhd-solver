package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solverhelper.PlotData;
import ru.vasily.solverhelper.misc.ArrayUtils;

import static ru.vasily.solver.Utils.*;
import static java.lang.Math.*;
import static ru.vasily.solverhelper.misc.ArrayUtils.*;
import static ru.vasily.solverhelper.PlotDataFactory.*;

public class MHDSolver1D implements MHDSolver
{

	private static final int VALUES_VECTOR_SIZE = 8;
	private final double[][] flow;
	private int count = 0;

	private double totalTime = 0;
	private final double[][] predictorData;
	private final double[][] correctorData;

	public double getTotalTime()
	{
		return totalTime;
	}

	public final int xRes;
	private final double GAMMA;
	private final double h;
	private final ThreePointRestorator restorator;
	private final double CFL;

	private final RiemannSolver riemannSolver;

	public MHDSolver1D(DataObject params, ThreePointRestorator restorator,
			RiemannSolver riemannSolver, double[][] initVals)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");
		xRes = calculationConstants.getInt("xRes");
		GAMMA = physicalConstants.getDouble("gamma");
		h = physicalConstants.getDouble("xLength") / xRes;
		CFL = calculationConstants.getDouble("CFL");
		this.riemannSolver = riemannSolver;
		this.restorator = restorator;
		flow = new double[xRes - 1][8];
		predictorData = initVals;
		correctorData = ArrayUtils.copy(predictorData);
	}

	public void nextTimeStep()
	{
		double tau = getTau();
		findPredictorFlow();
		applyStep(tau / 2, h, predictorData);
		findCorrectorFlow();
		applyStep(tau, h, correctorData);
		copy(predictorData, correctorData);
		count++;
		totalTime += tau;
	}

	private double getTau()
	{
		double tau = Double.POSITIVE_INFINITY;
		double[] u_phy = new double[8];
		for (int i = 0; i < xRes; i++)
		{
			toPhysical(u_phy, predictorData[i], GAMMA);
			double U = u_phy[1];
			double bX = u_phy[5];
			double currentSpeed = abs(U) + fastShockSpeed(u_phy, bX, GAMMA);
			tau = min(h / currentSpeed, tau);
		}
		return tau * CFL;
	}

	private void findPredictorFlow()
	{
		double[] uL_phy = new double[8];
		double[] uR_phy = new double[8];
		for (int i = 1; i < xRes - 2; i++)
		{
			double[] ul = predictorData[i];
			toPhysical(uL_phy, ul, GAMMA);
			double[] ur = predictorData[i + 1];
			toPhysical(uR_phy, ur, GAMMA);
			setFlow(flow[i], uL_phy, uR_phy, i);
		}
	}

	private void findCorrectorFlow()
	{
		double[] uL_phy = new double[8];
		double[] uR_phy = new double[8];
		double[] temp_1 = new double[8];
		double[] temp_2 = new double[8];
		double[] temp_3 = new double[8];
		for (int i = 1; i < xRes - 2; i++)
		{
			restoreLeft(uL_phy, i, temp_1, temp_2, temp_3);
			restoreRight(uR_phy, i, temp_1, temp_2, temp_3);
			setFlow(flow[i], uL_phy, uR_phy, i);
		}	
	}

	private void applyStep(double timeStep, double spaceStep, double[][] consVal)
	{
		for (int i = 2; i < xRes - 2; i++)
		{
			for (int k = 0; k < VALUES_VECTOR_SIZE; k++)
			{
				consVal[i][k] += (flow[i - 1][k] - flow[i][k]) * timeStep
						/ spaceStep;
			}
		}
	}

	private void restoreRight(double[] uR_phy, int i, double[] u_i,
			double[] u_i_plus_1, double[] u_i_plus_2)
	{
		_toPhysical(u_i, i);
		_toPhysical(u_i_plus_1, i + 1);
		_toPhysical(u_i_plus_2, i + 2);
		restore(uR_phy, u_i, u_i_plus_1, u_i_plus_2);
	}

	private void restoreLeft(double[] uL_phy, int i, double[] u_i_minus_1,
			double[] u_i, double[] u_i_plus_1)
	{
		_toPhysical(u_i_minus_1, i - 1);
		_toPhysical(u_i, i);
		_toPhysical(u_i_plus_1, i + 1);
		restore(uL_phy, u_i_plus_1, u_i, u_i_minus_1);
	}

	private void restore(double[] uR_phy, double[] u_i, double[] u_i_plus_1,
			double[] u_i_plus_2)
	{
		for (int k = 0; k < VALUES_VECTOR_SIZE; k++)
		{
			uR_phy[k] = restorator.restore(u_i[k], u_i_plus_1[k],
					u_i_plus_2[k]);
		}
	}

	private void _toPhysical(double[] u_phy, int i)
	{
		toPhysical(u_phy, predictorData[i], GAMMA);
	}

	private void setFlow(double[] flow, double[] uL, double[] uR, int i)
	{
		riemannSolver.getFlow(flow, uL, uR, GAMMA, GAMMA);
		if (isNAN(flow))
		{
			throw AlgorithmError.builder()
					.put("error type", "NAN value in flow")
					.put("total time", getTotalTime()).put("count", count)
					.put("i", i)
					.put("left_input", uL).put("right_input", uR)
					.put("output", flow)
					.build();
		}
	}

	public ImmutableMap<String, Object> getLogData()
	{
		return ImmutableMap.<String, Object> builder()
				.put("count", count)
				.put("total time", totalTime)
				.build();
	}

	public PlotData getData()
	{
		return plots(
				plot1D("density", getXCoord(), getPhysical(0)),
				plot1D("u", getXCoord(), getPhysical(1)),
				plot1D("v", getXCoord(), getPhysical(2)),
				plot1D("w", getXCoord(), getPhysical(3)),
				plot1D("thermal_pressure", getXCoord(), getPhysical(4)),
				plot1D("bY", getXCoord(), getPhysical(6)),
				plot1D("bZ", getXCoord(), getPhysical(7)),
				plot1D("density_flow", getXCoord(), getFlow(0)));

	}

	private double[] getPhysical(int valNum)
	{
		double[] ret = new double[xRes];
		double[] temp = new double[8];
		for (int i = 0; i < xRes; i++)
		{
			toPhysical(temp, predictorData[i], GAMMA);
			ret[i] = temp[valNum];
		}
		return ret;
	}

	private double[] getFlow(int valNum)
	{
		double[] ret = new double[xRes];
		for (int i = 0; i < xRes - 1; i++)
		{
			ret[i] = flow[i][valNum];
		}
		return ret;
	}

	private double[] getXCoord()
	{
		double[] ret = new double[xRes];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = i * h;
		}
		return ret;
	}

}
