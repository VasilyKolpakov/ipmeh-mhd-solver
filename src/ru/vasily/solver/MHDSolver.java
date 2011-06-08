package ru.vasily.solver;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import ru.vasily.dataobjs.DataObject;

public class MHDSolver {

	private static final String RHO = "rho";
	public double ro[];
	public double roU[];
	public double roV[];
	public double roW[];
	public double e[];
	public double bX[];
	public double bY[];
	public double bZ[];

	public double roPr[];
	public double roUPr[];
	public double roVPr[];
	public double roWPr[];
	public double ePr[];
	public double bXPr[];
	public double bYPr[];
	public double bZPr[];

	private final double[] uR_temp = new double[8];
	private final double[] uL_temp = new double[8];
	public double[][] flow;
	private int count = 0;

	private double totalTime = 0;

	public double getTotalTime() {
		return totalTime;
	}

	public final int xRes;
	private final double GAMMA;
	private final double h;
	private final double omega;
	private final double nu;
	private final double CFL;

	private final FlowRestorator flowRestorator;
	private final RiemannSolver riemannSolver;

	public MHDSolver(DataObject params) {
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");

		xRes = calculationConstants.getInt("xRes");
		GAMMA = physicalConstants.getDouble("gamma");
		h = physicalConstants.getDouble("xLenght") / xRes;
		omega = calculationConstants.getDouble("omega");
		nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");
		FlowRestorator.Fetcher fetcher = new FlowRestorator.Fetcher()
		{

			@Override
			public void setU(double[] arr, int i)
			{
				arr[0] = roPr[i];
				arr[1] = roUPr[i] / roPr[i];
				arr[2] = roVPr[i] / roPr[i];
				arr[3] = roWPr[i] / roPr[i];
				arr[4] = getPressurePr(i);
				arr[5] = bXPr[i];
				arr[6] = bYPr[i];
				arr[7] = bZPr[i];
			}

			@Override
			public void setDelta(double[] arr, int i)
			{
				arr[0] = roPr[i + 1] - roPr[i];
				arr[1] = roUPr[i + 1] / roPr[i + 1] - roUPr[i] / roPr[i];
				arr[2] = roVPr[i + 1] / roPr[i + 1] - roVPr[i] / roPr[i];
				arr[3] = roWPr[i + 1] / roPr[i + 1] - roWPr[i] / roPr[i];
				arr[4] = getPressurePr(i + 1) - getPressurePr(i);
				arr[5] = bXPr[i + 1] - bXPr[i];
				arr[6] = bYPr[i + 1] - bYPr[i];
				arr[7] = bZPr[i + 1] - bZPr[i];
			}
		};
		flowRestorator = new FlowRestorator(fetcher, nu, omega, 8);
		riemannSolver = new RoeSolverByKryukov();
		initMass();
		setInitData(params);
	}

	private void setInitData(DataObject params) {
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
		{
			ro[i] = rhoL;
			roU[i] = ro[i] * uL;
			roV[i] = ro[i] * vL;
			roW[i] = ro[i] * wL;
			e[i] = pL / (GAMMA - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
					+ (bYL * bYL + bZL * bZL + bX * bX) / 8 / Math.PI;
			this.bY[i] = bYL;
			this.bZ[i] = bZL;
			this.bX[i] = bX;
		}
		double rhoR = right.getDouble(RHO);
		double pR = right.getDouble("p");
		double uR = right.getDouble("u");
		double vR = right.getDouble("v");
		double wR = right.getDouble("w");
		double bYR = right.getDouble("bY");
		double bZR = right.getDouble("bZ");
		for (int i = middle; i < xRes; i++)
		{
			ro[i] = rhoR;
			roU[i] = ro[i] * uR;
			roV[i] = ro[i] * vR;
			roW[i] = ro[i] * wR;
			e[i] = pR / (GAMMA - 1) + rhoR * (uR * uR + vR * vR + wR * wR) / 2
					+ (bYR * bYR + bZR * bZR + bX * bX) / 8 / Math.PI;
			this.bY[i] = bYR;
			this.bZ[i] = bZR;
			this.bX[i] = bX;
		}
	}

	public ImmutableMap<String, String> getLogData() {
		Builder<String, String> mapBuilder = ImmutableMap.builder();
		mapBuilder.
				put("count", String.valueOf(count)).
				put("total time", String.valueOf(totalTime));
		return mapBuilder.build();
	}

	public ImmutableMap<String, double[]> getData() {
		Builder<String, double[]> mapBuilder = ImmutableMap.builder();
		double[] u = new double[xRes];
		double[] v = new double[xRes];
		double[] w = new double[xRes];
		double[] p = new double[xRes];

		for (int i = 0; i < xRes; i++)
		{
			u[i] = roU[i] / ro[i];
			v[i] = roV[i] / ro[i];
			w[i] = roW[i] / ro[i];
			p[i] = getPressure(i);
		}
		return mapBuilder.
				put("density", ro).
				put("thermal_pressure", p).
				put("u", u).
				put("v", v).
				put("w", w).
				put("bY", bY).
				put("bZ", bZ).
				build();
	}

	public double[] getXCoord() {
		double[] ret = new double[xRes];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = i * h;
		}
		return ret;
	}

	public void nextTimeStep() {
		copyArrays(ro, roPr, roU, roUPr, roV, roVPr, roW, roWPr, e, ePr, bX,
				bXPr, bY, bYPr, bZ, bZPr);
		findPredictorFlows();
		double tau = getTau();
		applyStep(tau / 2, h, roPr, roUPr, roVPr, roWPr, ePr, bXPr, bYPr, bZPr);
		findCorrectorFlows();
		applyStep(tau, h, ro, roU, roV, roW, e, bX, bY, bZ);
		count++;
		totalTime += tau;
	}

	private double getTau() {
		double tau = Double.POSITIVE_INFINITY;
		for (int i = 0; i < xRes; i++)
		{
			double b_square_div4piRo = (bX[i] * bX[i] + bY[i] * bY[i] + bZ[i]
					* bZ[i])
					/ (4 * Math.PI * ro[i]);
			double speedOfSound_square = GAMMA * getPressure(i) / ro[i];
			double speedOfSound = Math.sqrt(speedOfSound_square);
			double absBx = Math.abs(bX[i]);
			double third = absBx * speedOfSound / Math.sqrt(Math.PI * ro[i]);
			double cf = 0.5 * (Math.sqrt(speedOfSound_square
					+ b_square_div4piRo + third) + Math
					.sqrt(speedOfSound_square + b_square_div4piRo - third));
			double currentSpeed = Math.abs(roU[i] / ro[i]) + cf;
			tau = Math.min(h / currentSpeed, tau);
		}
		return tau * CFL;
	}

	private void copyArrays(double[]... arrays) {
		if (arrays.length % 2 != 0)
			throw new IllegalArgumentException("arrays.length%2!=0");
		for (int i = 0; i < arrays.length / 2; i++)
		{
			System.arraycopy(arrays[2 * i], 0, arrays[2 * i + 1], 0,
					arrays[2 * i].length);
		}
	}

	private void applyStep(double timeStep, double spaceStep, double[]... var) {
		for (int k = 0; k < var.length; k++)
		{
			for (int i = 1; i < xRes - 1; i++)
			{
				var[k][i] += (flow[i - 1][k] - flow[i][k]) * timeStep
						/ spaceStep;
			}
		}
	}

	private void findPredictorFlows() {
		for (int i = 0; i < xRes - 1; i++)
		{
			double RhoL = ro[i];
			double UL = roU[i] / ro[i];
			double VL = roV[i] / ro[i];
			double WL = roW[i] / ro[i];
			double PGasL = getPressure(i);
			double BXL = bX[i];
			double BYL = bY[i];
			double BZL = bZ[i];
			double GamL = GAMMA;

			double RhoR = ro[i + 1];
			double UR = roU[i + 1] / ro[i + 1];
			double VR = roV[i + 1] / ro[i + 1];
			double WR = roW[i + 1] / ro[i + 1];
			double PGasR = getPressure(i + 1);
			double BXR = bX[i + 1];
			double BYR = bY[i + 1];
			double BZR = bZ[i + 1];
			double GamR = GAMMA;

			setCheckedFlow(flow, i, RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL,
					GamL, RhoR, UR, VR, WR, PGasR, BXR, BYR, BZR, GamR);
		}
	}

	private void findCorrectorFlows() {
		for (int i = 1; i < xRes - 2; i++)
		{

			double[] uR = uR_temp;
			double[] uL = uL_temp;

			flowRestorator.setRestoredURandUL(uR, uL, i);

			double RhoL = uL[0];
			double UL = uL[1];
			double VL = uL[2];
			double WL = uL[3];
			double PGasL = uL[4];
			double BXL = uL[5];
			double BYL = uL[6];
			double BZL = uL[7];
			double GamL = GAMMA;

			double RhoR = uR[0];
			double UR = uR[1];
			double VR = uR[2];
			double WR = uR[3];
			double PGasR = uR[4];
			double BXR = uR[5];
			double BYR = uR[6];
			double BZR = uR[7];
			double GamR = GAMMA;
			
			setCheckedFlow(flow, i, RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL,
					GamL, RhoR, UR, VR, WR, PGasR, BXR, BYR, BZR, GamR);
		}
	}

	private double getPressure(int i) {
		double p = (e[i]
				- (roU[i] * roU[i] + roV[i] * roV[i] + roW[i] * roW[i]) / ro[i]
				/ 2 - (bX[i] * bX[i] + bY[i] * bY[i] + bZ[i] * bZ[i]) / 8
				/ Math.PI)
				* (GAMMA - 1);
		return p;
	}

	private double getPressurePr(int i) {
		double p = (ePr[i]
				- (roUPr[i] * roUPr[i] + roVPr[i] * roVPr[i] + roWPr[i]
						* roWPr[i]) / roPr[i] / 2 - (bXPr[i] * bXPr[i]
				+ bYPr[i] * bYPr[i] + bZPr[i] * bZPr[i])
				/ 8 / Math.PI)
				* (GAMMA - 1);
		return p;
	}

	private void setCheckedFlow(double[][] flow, int i, double RhoL, double UL,
			double VL, double WL, double PGasL, double BXL, double BYL,
			double BZL, double GamL, double RhoR, double UR, double VR,
			double WR, double PGasR, double BXR, double BYR, double BZR,
			double GamR) {
		riemannSolver.getFlow(flow[i], RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL, GamL, RhoR,
				UR, VR, WR, PGasR, BXR, BYR, BZR, GamR);
		if (checkIsNAN(flow[i]))
		{
			Map<String, Double> leftInput = ImmutableMap.<String, Double> builder().
					put("RhoL", RhoL).
					put("UL", UL).
					put("VL", VL).
					put("WL", WL).
					put("PGasL", PGasL).
					put("BXL", BXL).
					put("BYL", BYL).
					put("BZL", BZL).
					put("GamL", GamL).build();
			Map<String, Double> rightInput = ImmutableMap.<String, Double> builder().
					putAll(ImmutableMap.of("RhoR", RhoR, "UR", UR, "VR", VR,
							"WR", WR, "PGasR", PGasR)).
					putAll(ImmutableMap.of("BXR", BXR, "BYR", BYR, "BZR",
							BZR, "GamR", GamR)).build();

			throw AlgorithmError.builder()
					.put("error type", "NAN value in flow")
					.put("total time", getTotalTime()).put("count", count)
					.put("x coordinate", i * h).put("i", i)
					.put("left_input", leftInput)
					.put("right_input", rightInput)
					.put("output", flow[i])
					.build();
		}

	}

	private void initMass() {
		ro = new double[xRes];
		roU = new double[xRes];
		roV = new double[xRes];
		roW = new double[xRes];
		e = new double[xRes];
		bX = new double[xRes];
		bY = new double[xRes];
		bZ = new double[xRes];

		roPr = new double[xRes];
		roUPr = new double[xRes];
		roVPr = new double[xRes];
		roWPr = new double[xRes];
		ePr = new double[xRes];
		bXPr = new double[xRes];
		bYPr = new double[xRes];
		bZPr = new double[xRes];

		flow = new double[xRes - 1][8];
	}

	private boolean checkIsNAN(double[] arr) {
		for (int i = 0; i < arr.length; i++)
		{
			if (Double.isNaN(arr[i]))
				return true;
		}
		return false;
	}

}
