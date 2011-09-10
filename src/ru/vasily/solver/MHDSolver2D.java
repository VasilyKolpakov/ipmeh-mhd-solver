package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solverhelper.PlotData;

import static ru.vasily.solver.Utils.*;
import static java.lang.Math.*;
import static ru.vasily.solverhelper.misc.ArrayUtils.*;
import static ru.vasily.solverhelper.PlotDataFactory.*;

public class MHDSolver2D implements MHDSolver
{
	private enum Coordinate {
		X, Y
	}

	private static final String RHO = "rho";

	private int count = 0;

	private double totalTime = 0;
	private final double[][][] consVal;
	private final double[][][] left_right_flow;
	private final double[][][] up_down_flow;

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
	// private final double omega;
	// private final double nu;
	private final double CFL;
	private final Coordinate c = Coordinate.X;

	private final RiemannSolver2D riemannSolver;

	public MHDSolver2D(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");

		xRes = calculationConstants.getInt("xRes");
		yRes = calculationConstants.getInt("yRes");
		hx = physicalConstants.getDouble("xLenght") / xRes;
		hy = physicalConstants.getDouble("yLenght") / yRes;
		gamma = physicalConstants.getDouble("gamma");
		// omega = calculationConstants.getDouble("omega");
		// nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");
		riemannSolver = new RiemannSolver1Dto2DWrapper(new RoeSolverByKryukov());
		consVal = new double[xRes][yRes][8];
		left_right_flow = new double[xRes][yRes][8];
		up_down_flow = new double[xRes][yRes][8];
		setInitData(params);
	}

	private void setInitData(DataObject params)
	{
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");

		double rhoL = left.getDouble(RHO);
		double pL = left.getDouble("p");
		double uL = left.getDouble("u");
		double vL = left.getDouble("v");
		double wL = left.getDouble("w");
		double bXL = left.getDouble("bX");
		double bYL = left.getDouble("bY");
		double bZL = left.getDouble("bZ");
		final int x_1_finish;
		final int y_1_finish;
		final int x_2_start;
		final int y_2_start;
		if (c.equals(Coordinate.X))
		{
			x_1_finish = (int) (xRes * (physicalConstants.getDouble("xMiddlePoint") / physicalConstants
					.getDouble("xLenght")));
			y_1_finish = yRes;
			x_2_start = x_1_finish;
			y_2_start = 0;

		}
		else
		{
			x_1_finish = xRes;
			y_1_finish = (int) (yRes * (physicalConstants.getDouble("yMiddlePoint") / physicalConstants
					.getDouble("yLenght")));
			x_2_start = 0;
			y_2_start = y_1_finish;
		}
		for (int i = 0; i < x_1_finish; i++)
			for (int j = 0; j < y_1_finish; j++)
			{
				double[] u = consVal[i][j];
				u[0] = rhoL;
				u[1] = rhoL * uL;
				u[2] = rhoL * vL;
				u[3] = rhoL * wL;
				u[4] = pL / (gamma - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
						+ (bYL * bYL + bZL * bZL + bXL * bXL) / 8 / PI;
				u[5] = bXL;
				u[6] = bYL;
				u[7] = bZL;
			}
		double rhoR = right.getDouble(RHO);
		double pR = right.getDouble("p");
		double uR = right.getDouble("u");
		double vR = right.getDouble("v");
		double wR = right.getDouble("w");
		double bXR = right.getDouble("bX");
		double bYR = right.getDouble("bY");
		double bZR = right.getDouble("bZ");
		for (int i = x_2_start; i < xRes; i++)
			for (int j = y_2_start; j < yRes; j++)
			{
				double[] u = consVal[i][j];
				u[0] = rhoR;
				u[1] = rhoR * uR;
				u[2] = rhoR * vR;
				u[3] = rhoR * wR;
				u[4] = pR / (gamma - 1) + rhoR * (uR * uR + vR * vR + wR * wR) / 2
						+ (bYR * bYR + bZR * bZR + bXR * bXR) / 8 / PI;
				u[5] = bXR;
				u[6] = bYR;
				u[7] = bZR;
			}
	}

	@Override
	public void nextTimeStep()
	{
		double tau = getTau();
		findPredictorFlow();
		applyStep(tau, consVal);
		count++;
		totalTime += tau;
	}

	private double getTau()
	{
		double tau = Double.POSITIVE_INFINITY;
		double[] u_phy = new double[8];
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(u_phy, consVal[i][j], gamma);
				double U = u_phy[1];
				double V = u_phy[2];
				double bX = u_phy[5];
				double bY = u_phy[6];
				double currentSpeed = abs(c.equals(Coordinate.X) ? U : V)
						+ fastShockSpeed(u_phy, (c.equals(Coordinate.X) ? bX : bY), gamma);
				tau = min((c.equals(Coordinate.X) ? hx : hy) / currentSpeed, tau);
			}
		return tau * CFL;
	}

	private void findPredictorFlow()
	{
		double[] uL_phy = new double[8];
		double[] uR_phy = new double[8];
		for (int i = 0; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 1; j++)
			{
				double[] ul = consVal[i][j];
				toPhysical(uL_phy, ul, gamma);
				double[] ur = consVal[i + 1][j];
				toPhysical(uR_phy, ur, gamma);
				double[] flow = left_right_flow[i][j];
				setFlow(flow, uL_phy, uR_phy, i, j, 1.0, 0.0);
			}
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 0; j < yRes - 1; j++)
			{
				double[] ul = consVal[i][j];
				toPhysical(uL_phy, ul, gamma);
				double[] ur = consVal[i][j + 1];
				toPhysical(uR_phy, ur, gamma);
				double[] flow = up_down_flow[i][j];
				setFlow(flow, uL_phy, uR_phy, i, j, 0.0, 1.0);
			}

	}

	private void applyStep(double timeStep, double[][][] consVal)
	{
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 1; j < yRes - 1; j++)
				for (int k = 0; k < 8; k++)
				{
					final double up_down_diff = up_down_flow[i][j - 1][k]
							- up_down_flow[i][j][k];
					consVal[i][j][k] += up_down_diff
							* timeStep / hy;

					final double left_right_diff = left_right_flow[i - 1][j][k] -
							left_right_flow[i][j][k];
					consVal[i][j][k] += left_right_diff
							* timeStep / hx;
				}
	}

	private void setFlow(double[] flow, double[] uL, double[] uR, int i, int j, double cos_alfa, double sin_alfa)
	{
		riemannSolver.getFlow(flow, uL, uR, gamma, gamma, cos_alfa, sin_alfa);
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
		return ImmutableMap.<String, Object> builder()
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
		double[][][] phy = getPhysical(consVal);
		return plots(
				plot1D("density", getXCoord(), getSlice(phy, c, 0)),
				plot1D("u", getXCoord(), getSlice(phy, c, 1)),
				plot1D("v", getXCoord(), getSlice(phy, c, 2)),
				plot1D("w", getXCoord(), getSlice(phy, c, 3)),
				plot1D("thermal_pressure", getXCoord(), getSlice(phy, c, 4)),
				plot1D("bY", getXCoord(), getSlice(phy, c, 6)),
				plot1D("bZ", getXCoord(), getSlice(phy, c, 7)),
				plot1D("ro_flow", getXCoord(), getSlice(up_down_flow, c, 0)));
	}

	private double[] getXCoord()
	{
		double[] ret = new double[(c.equals(Coordinate.X) ? xRes : yRes)];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = i * (c.equals(Coordinate.X) ? hx : hy);
		}
		return ret;
	}

	private double[] getSlice(double[][][] physical, Coordinate c, int valNum) {
		switch (c) {
		case X:
			return getXSlice(physical, valNum);
		case Y:
			return getYSlice(physical, valNum);
		}
		throw new RuntimeException("coordinate is neither x nor y");
	}

	private double[][][] getPhysical(double[][][] consVal)
	{
		double[][][] ret = new double[consVal.length][consVal[0].length][consVal[0][0].length];
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(ret[i][j], consVal[i][j], gamma);
			}
		return ret;
	}

	private double[] getXSlice(double[][][] physical, int valNum)
	{
		double[] ret = new double[physical.length];
		int yCenter = physical[0].length / 2;
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = physical[i][yCenter][valNum];
		}
		return ret;
	}

	private double[] getYSlice(double[][][] physical, int valNum)
	{
		double[] ret = new double[physical[0].length];
		int xCenter = physical.length / 2;
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = physical[xCenter][i][valNum];
		}
		return ret;
	}

}
