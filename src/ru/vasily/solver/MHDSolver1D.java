package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;

import static ru.vasily.solver.Utils.*;
import static java.lang.Math.*;
import static ru.vasily.solverhelper.misc.ArrayUtils.*;

public class MHDSolver1D implements MHDSolver
{

	private static final String RHO = "rho";

	private final double[][] flow;
	private int count = 0;

	private double totalTime = 0;
	private final double[][] consVal;

	public double getTotalTime()
	{
		return totalTime;
	}

	public final int xRes;
	private final double GAMMA;
	private final double h;
	// private final double omega;
	// private final double nu;
	private final double CFL;

	private final RiemannSolver riemannSolver;

	public MHDSolver1D(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");

		xRes = calculationConstants.getInt("xRes");
		GAMMA = physicalConstants.getDouble("gamma");
		h = physicalConstants.getDouble("xLenght") / xRes;
		// omega = calculationConstants.getDouble("omega");
		// nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");
		riemannSolver = new RoeSolverByKryukov();
		flow = new double[xRes - 1][8];
		consVal = new double[xRes][8];
		setInitData(params);
	}

	private void setInitData(DataObject params)
	{
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");
		int middle = (int) (xRes * (physicalConstants.getDouble("xMiddlePoint") / physicalConstants
				.getDouble("xLenght")));
		{
			double rhoL = left.getDouble(RHO);
			double pL = left.getDouble("p");
			double uL = left.getDouble("u");
			double vL = left.getDouble("v");
			double wL = left.getDouble("w");
			double bXL = left.getDouble("bX");
			double bYL = left.getDouble("bY");
			double bZL = left.getDouble("bZ");
			for (int i = 0; i < middle; i++)
			{
				double[] u = consVal[i];
				u[0] = rhoL;
				u[1] = rhoL * uL;
				u[2] = rhoL * vL;
				u[3] = rhoL * wL;
				u[4] = pL / (GAMMA - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
						+ (bYL * bYL + bZL * bZL + bXL * bXL) / 8 / PI;
				u[5] = bYL;
				u[6] = bZL;
				u[7] = bXL;
			}
		}
		{
			double rhoR = right.getDouble(RHO);
			double pR = right.getDouble("p");
			double uR = right.getDouble("u");
			double vR = right.getDouble("v");
			double wR = right.getDouble("w");
			double bXR = right.getDouble("bX");
			double bYR = right.getDouble("bY");
			double bZR = right.getDouble("bZ");
			for (int i = middle; i < xRes; i++)
			{
				double[] u = consVal[i];
				u[0] = rhoR;
				u[1] = rhoR * uR;
				u[2] = rhoR * vR;
				u[3] = rhoR * wR;
				u[4] = pR / (GAMMA - 1) + rhoR * (uR * uR + vR * vR + wR * wR) / 2
						+ (bYR * bYR + bZR * bZR + bXR * bXR) / 8 / PI;
				u[5] = bYR;
				u[6] = bZR;
				u[7] = bXR;
			}
		}
	}

	public void nextTimeStep()
	{
		double tau = getTau();
		findPredictorFlow();
		applyStep(tau, h, consVal);
		count++;
		totalTime += tau;
	}

	private double getTau()
	{
		double tau = Double.POSITIVE_INFINITY;
		double[] u_phy = new double[8];
		for (int i = 0; i < xRes; i++)
		{
			toPhysical(u_phy, consVal[i], GAMMA);
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
		for (int i = 0; i < xRes - 1; i++)
		{
			double[] ul = consVal[i];
			toPhysical(uL_phy, ul, GAMMA);
			double[] ur = consVal[i + 1];
			toPhysical(uR_phy, ur, GAMMA);
			setFlow(flow[i], uL_phy, uR_phy, i);
		}
	}

	private void applyStep(double timeStep, double spaceStep, double[][] consVal)
	{
		for (int i = 1; i < xRes - 1; i++)
		{
			for (int k = 0; k < 8; k++)
			{
				consVal[i][k] += (flow[i - 1][k] - flow[i][k]) * timeStep
						/ spaceStep;
			}
		}
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
			toPhysical(temp, consVal[i], GAMMA);
			ret[i] = temp[valNum];
		}
		return ret;
	}

	public double[] getXCoord()
	{
		double[] ret = new double[xRes];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = i * h;
		}
		return ret;
	}

}
