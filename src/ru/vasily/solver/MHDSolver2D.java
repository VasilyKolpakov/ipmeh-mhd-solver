package ru.vasily.solver;

import static com.google.common.base.Preconditions.*;
import static java.lang.Math.*;
import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solverhelper.misc.ArrayUtils;

public class MHDSolver2D implements MHDSolver
{
	private final double[][][] consVal;
	private final double[][][] left_right_flow;
	private final double[][][] up_down_flow;

	private final int xRes;
	private final int yRes;
	private final double hx;
	private final double hy;
	private final double gamma;
	private final double omega;
	private final double nu;
	private final double CFL;

	private final RiemannSolver riemannSolver;
	private final TreePointRestorator restorator;
	private double totalTime = 0;
	private int count = 0;

	public MHDSolver2D(DataObject parameters)
	{
		DataObject calculationConstants = parameters
				.getObj("calculationConstants");
		DataObject physicalConstants = parameters.getObj("physicalConstants");
		xRes = calculationConstants.getInt("xRes");
		yRes = calculationConstants.getInt("yRes");
		gamma = physicalConstants.getDouble("gamma");
		hx = physicalConstants.getDouble("xLenght") / xRes;
		hy = physicalConstants.getDouble("yLenght") / yRes;
		omega = calculationConstants.getDouble("omega");
		nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");

		riemannSolver = new RoeSolverByKryukov();
		restorator = new SimpleRestorator();

		consVal = new double[xRes][yRes][8];
		up_down_flow = new double[xRes][yRes][8];
		left_right_flow = new double[xRes][yRes][8];
		setInitialValues(parameters);
	}

	private void setInitialValues(DataObject params)
	{
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");
		double bX = physicalConstants.getDouble("bX");

		double rhoL = left.getDouble("rho");
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
				u[4] = pL / (gamma - 1) + rhoL * (uL * uL + vL * vL + wL * wL)
						/ 2
						+ (bYL * bYL + bZL * bZL + bX * bX) / 8 / Math.PI;
				u[5] = bYL;
				u[6] = bZL;
				u[7] = bX;
			}
		double rhoR = right.getDouble("rho");
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
				u[4] = pR / (gamma - 1) + rhoR * (uR * uR + vR * vR + wR * wR)
						/ 2
						+ (bYR * bYR + bZR * bZR + bX * bX) / 8 / Math.PI;
				u[5] = bYR;
				u[6] = bZR;
				u[7] = bX;
			}
	}

	public void nextTimeStep()
	{
		calculateFlow(left_right_flow, up_down_flow, consVal);
		double tau = getTau();
		applyStep(tau, left_right_flow, up_down_flow, consVal);
		totalTime += tau;
		count++;
	}

	private void applyStep(double tau, double[][][] left_right_flow,
			double[][][] up_down_flow, double[][][] consVal)
	{
		for (int i = 1; i < xRes; i++)
			for (int j = 1; j < yRes; j++)
				for (int k = 0; k < 8; k++)
				{
					// consVal[i][j][k] += (up_down_flow[i][j - 1][k]
					// - up_down_flow[i][j][k])
					// * tau / hy;
					consVal[i][j][k] += (left_right_flow[i - 1][j][k]
							- left_right_flow[i][j][k])
							* tau / hx;
				}
	}

	private void calculateFlow(double[][][] left_right_flow,
			double[][][] up_down_flow, double[][][] consVal)
	{
		double[] u_physical_l = new double[8];
		double[] u_physical_r = new double[8];
		for (int i = 0; i < xRes - 1; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(u_physical_l, consVal[i][j]);
				toPhysical(u_physical_r, consVal[i + 1][j]);
				setFlow(left_right_flow[i][j], u_physical_l, u_physical_r, 1.0,
						0.0, i, j,
						"left_right_flow");
			}
		// for (int i = 0; i < xRes; i++)
		// for (int j = 0; j < yRes - 1; j++)
		// {
		// toPhysicalY(u_physical_l, consVal[i][j]);
		// toPhysicalY(u_physical_r, consVal[i][j + 1]);
		// setFlow(up_down_flow[i][j], u_physical_l, u_physical_r, 0.0, 1.0, i,
		// j,
		// "up_down_flow");
		// }
	}

	private double getTau()
	{
		double tau = Double.POSITIVE_INFINITY;
		double[] u_phy = new double[8];
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(u_phy, consVal[i][j]);
				double ro = u_phy[0];
				double U = u_phy[1];
				double V = u_phy[2];
				double W = u_phy[3];
				double PGas = u_phy[4];
				double bX = u_phy[5];
				double bY = u_phy[6];
				double bZ = u_phy[7];

				double b_square_div4piRo = (bX * bX + bY * bY + bZ
						* bZ)
						/ (4 * PI * ro);
				double speedOfSound_square = gamma * PGas / ro;
				double speedOfSound = sqrt(speedOfSound_square);
				double absBx = abs(bX);
				double third = absBx * speedOfSound / sqrt(PI * ro);
				double cf = 0.5 * (sqrt(speedOfSound_square
						+ b_square_div4piRo + third) + sqrt(speedOfSound_square
						+ b_square_div4piRo
						- third));
				double currentSpeed = abs(U) + cf;
				tau = min(hx / currentSpeed, tau);
				if (Double.isNaN(tau))
				{
					throw AlgorithmError.builder()
							.put("error type", "NAN value in tau")
							.put("total time", getTotalTime())
							.put("count", count)
							.put("i", i).put("j", j)
							.put("rho", ro)
							.put("Ux", U)
							.put("Uy", V)
							.put("Uz", W)
							.put("P", PGas)
							.put("Bx", bX)
							.put("By", bY)
							.put("Bz", bZ)
							.build();
				}
			}
		return tau * CFL;
	}

	private void setFlow(double[] flow, double[] uL, double[] uR,
			double cos_alfa, double sin_alfa, int i, int j, String comment)
	{
		double alfa_length = cos_alfa * cos_alfa + sin_alfa * sin_alfa;
		checkArgument(abs(alfa_length - 1.0) < 0.000000000001,
				"alfa length is %s not 1.0",
				alfa_length);
		RiemannUtils.getFlow(riemannSolver, flow, uL, uR, gamma, gamma,
				cos_alfa, sin_alfa);
		if (ArrayUtils.isNAN(flow))
		{
			throw AlgorithmError.builder()
					.put("error type", "NAN value in flow")
					.put("total time", getTotalTime()).put("count", count)
					.put("comment", comment)
					.put("i", i).put("j", j)
					.put("alfa_re", cos_alfa).put("alfa_im", sin_alfa)
					.put("left_input", uL).put("right_input", uR)
					.put("output", flow)
					.build();
		}
	}

	private void toPhysical(double[] result, double[] u)
	{
		double ro = u[0];
		double roU = u[1];
		double roV = u[2];
		double roW = u[3];

		double bX = u[5];
		double bY = u[6];
		double bZ = u[7];

		double Rho = ro;
		double U = roU / ro;
		double V = roV / ro;
		double W = roW / ro;
		double PGas = getPressure(u);
		double BX = bX;
		double BY = bY;
		double BZ = bZ;
		result[0] = Rho;
		result[1] = U;
		result[2] = V;
		result[3] = W;
		result[4] = PGas;
		result[5] = BX;
		result[6] = BY;
		result[7] = BZ;
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
		double bZ = u[7];
		double p = (e
				- (roU * roU + roV * roV + roW * roW) / ro
				/ 2 - (bX * bX + bY * bY + bZ * bZ) / 8
				/ Math.PI)
				* (gamma - 1);
		return p;
	}

	public double getTotalTime()
	{
		return totalTime;
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

		double[][][] phy = getPhysical(consVal);
		return ImmutableMap.<String, double[]> builder().
				put("ro_x", getXSlice(phy, 0)).
				// put("ro_y", getYSlice(phy, 0)).
				build();
	}

	public double[] getXCoord()
	{
		double[] xCoord = new double[xRes];
		for (int i = 0; i < xRes; i++)
		{
			xCoord[i] = i * hx;
		}
		return xCoord;
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

	private double[][][] getPhysical(double[][][] consVal)
	{
		double[][][] ret = new double[consVal.length][consVal[0].length][consVal[0][0].length];
		for (int i = 0; i < xRes; i++)
			for (int j = 0; j < yRes; j++)
			{
				toPhysical(ret[i][j], consVal[i][j]);
			}
		return ret;
	}
}
