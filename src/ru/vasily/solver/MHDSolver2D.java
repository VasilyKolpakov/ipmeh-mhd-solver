package ru.vasily.solver;

import ru.vasily.dataobjs.DataObject;

public class MHDSolver2D
{
	double[][][] consVal;
	double[][][] left_right_flow;
	double[][][] up_down_flow;

	private final int xRes;
	private final int yRes;
	private final double h;
	private final double gamma;
	private final double omega;
	private final double nu;
	private final double CFL;

	private final RiemannSolver riemannSolver;
	private double tau;

	public MHDSolver2D(DataObject parameters)
	{
		DataObject calculationConstants = parameters
				.getObj("calculationConstants");
		DataObject physicalConstants = parameters.getObj("physicalConstants");

		xRes = calculationConstants.getInt("xRes");
		yRes = calculationConstants.getInt("yRes");
		gamma = physicalConstants.getDouble("gamma");
		h = physicalConstants.getDouble("xLenght") / xRes;
		omega = calculationConstants.getDouble("omega");
		nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");
		riemannSolver = new RoeSolverByKryukov();
		consVal = new double[xRes][yRes][8];
		up_down_flow = new double[xRes][yRes][8];
		left_right_flow = new double[xRes][yRes][8];
	}

	private void applyStep()
	{
		double cellArea = 1;
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
				for (int k = 0; k < 8; k++)
				{
					consVal[i][j][k] += (up_down_flow[i][j - 1][k]
															- up_down_flow[i][j][k])
							* tau / cellArea;
					consVal[i][j][k] += (left_right_flow[i - 1][j][k]
															- left_right_flow[i][j][k])
							* tau / cellArea;
				}

	}

	private void calculateFlow(double[][][] left_right_flow,
			double[][][] up_down_flow, double[][][] consVal)
	{
		for (int i = 0; i < consVal.length; i++)
			for (int j = 0; j < consVal.length; j++)
			{
				double[] u = consVal[i][j];
				double ro = u[0];
				double roU = u[1];
				double roV = u[2];
				double roW = u[3];
				double e = u[4];
				double bX = u[5];
				double bY = u[6];
				double bZ = u[8];
				double RhoL = ro;
				double UL = roU / ro;
				double VL = roV / ro;
				double WL = roW / ro;
				double PGasL = getPressure(u);
				double BXL = bX;
				double BYL = bY;
				double BZL = bZ;
				double GamL = gamma;
			}

	}

	private double getPressure(double[] u)
	{
		double ro = u[0];
		double roU = u[1];
		double roV = u[2];
		double roW = u[3];
		double e = u[4];
		double bX = u[5];
		double bY = u[6];
		double bZ = u[8];
		double p = (e
				- (roU * roU + roV * roV + roW * roW) / ro
				/ 2 - (bX * bX + bY * bY + bZ * bZ) / 8
				/ Math.PI)
				* (gamma - 1);
		return p;
	}
}
