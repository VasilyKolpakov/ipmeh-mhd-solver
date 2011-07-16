package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;

import static ru.vasily.solver.Utils.*;
import static java.lang.Math.*;
import static ru.vasily.solverhelper.misc.ArrayUtils.*;

public class MHDSolver2D implements MHDSolver
{

	private static final String RHO = "rho";

	private int count = 0;

	private double totalTime = 0;
	private final double[][][] consVal;
	private final double[][][] left_right_flow;

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
		setInitData(params);
	}

	private void setInitData(DataObject params)
	{
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");
		double bX = physicalConstants.getDouble("bX");

		double rhoL = left.getDouble(RHO);
		double pL = left.getDouble("p");
		double uL = left.getDouble("u");
		double vL = left.getDouble("v");
		double wL = left.getDouble("w");
		double bYL = left.getDouble("bY");
		double bZL = left.getDouble("bZ");
		int middle = (int) (xRes * (physicalConstants.getDouble("xMiddlePoint") / physicalConstants
				.getDouble("xLenght")));
		for (int i = 0; i < middle; i++)

			for (int j = 0; j < yRes; j++)
			{
				double[] u = consVal[i][j];
				u[0] = rhoL;
				u[1] = rhoL * uL;
				u[2] = rhoL * vL;
				u[3] = rhoL * wL;
				u[4] = pL / (gamma - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
						+ (bYL * bYL + bZL * bZL + bX * bX) / 8 / PI;
				u[5] = bYL;
				u[6] = bZL;
				u[7] = bX;
			}
		double rhoR = right.getDouble(RHO);
		double pR = right.getDouble("p");
		double uR = right.getDouble("u");
		double vR = right.getDouble("v");
		double wR = right.getDouble("w");
		double bYR = right.getDouble("bY");
		double bZR = right.getDouble("bZ");
		for (int i = middle; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				double[] u = consVal[i][j];
				u[0] = rhoR;
				u[1] = rhoR * uR;
				u[2] = rhoR * vR;
				u[3] = rhoR * wR;
				u[4] = pR / (gamma - 1) + rhoR * (uR * uR + vR * vR + wR * wR) / 2
						+ (bYR * bYR + bZR * bZR + bX * bX) / 8 / PI;
				u[5] = bYR;
				u[6] = bZR;
				u[7] = bX;
			}
	}

	public void nextTimeStep()
	{
		double tau = getTau();
		findPredictorFlow();
		applyStep(tau, hx, consVal);
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
				double bX = u_phy[5];
				double currentSpeed = abs(U) + fastShockSpeed(u_phy, bX, gamma);
				tau = min(hx / currentSpeed, tau);
			}
		return tau * CFL;
	}

	private void findPredictorFlow()
	{
		double[] uL_phy = new double[8];
		double[] uR_phy = new double[8];
		for (int i = 0; i < xRes - 1; i++)
			for (int j = 0; j < yRes; j++)
			{
				double[] ul = consVal[i][j];
				toPhysical(uL_phy, ul, gamma);
				double[] ur = consVal[i + 1][j];
				toPhysical(uR_phy, ur, gamma);
				setFlow(left_right_flow[i][j], uL_phy, uR_phy, i);
			}
	}

	private void applyStep(double timeStep, double spaceStep, double[][][] consVal)
	{
		for (int i = 1; i < xRes - 1; i++)
			for (int j = 0; j < yRes; j++)
				for (int k = 0; k < 8; k++)
				{
					consVal[i][j][k] += (left_right_flow[i - 1][j][k] - left_right_flow[i][j][k])
							* timeStep
							/ spaceStep;
				}
	}

	private void setFlow(double[] flow, double[] uL, double[] uR, int i)
	{
		riemannSolver.getFlow(flow, uL, uR, gamma, gamma, 1.0, 0.0);
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

	public ImmutableMap<String, double[]> getData()
	{
		return ImmutableMap.<String, double[]> builder().
				put("density", getPhysical(0)).
				put("u", getPhysical(1)).
				put("v", getPhysical(2)).
				put("w", getPhysical(3)).
				put("thermal_pressure", getPhysical(4)).
				put("bY", getPhysical(6)).
				put("bZ", getPhysical(7)).
				build();

	}

	private double[] getPhysical(int valNum)
	{
		double[] ret = new double[xRes];
		double[] temp = new double[8];
		for (int i = 0; i < xRes; i++)
		{
			toPhysical(temp, consVal[i][yRes / 2], gamma);
			ret[i] = temp[valNum];
		}
		return ret;
	}

	public double[] getXCoord()
	{
		double[] ret = new double[xRes];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = i * hx;
		}
		return ret;
	}

}
