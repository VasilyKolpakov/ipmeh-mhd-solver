package ru.vasily.solver;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import ru.vasily.dataobjs.DataObj;
import ru.vasily.dataobjs.InitialValues;
import ru.vasily.dataobjs.Parameters;

public class MHDSolver {

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

	private final int xRes;
	private final double GAMMA;
	private final double h;
	private final double omega;
	private final double nu;
	private final double CFL;

	private final FlowRestorator flowRestorator;

	public MHDSolver(Parameters params) {
		xRes = params.calculationConstants.xRes;
		GAMMA = params.physicalConstants.gamma;
		h = params.physicalConstants.xLenght / xRes;
		omega = params.calculationConstants.omega;
		nu = params.calculationConstants.nu;
		CFL = params.calculationConstants.CFL;
		FlowRestorator.Fetcher fetcher = new FlowRestorator.Fetcher() {

			@Override
			public void setU(double[] arr, int i) {
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
			public void setDelta(double[] arr, int i) {
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
		initMass();
		setInitData(params);
	}

	private void setInitData(Parameters params) {
		InitialValues left = params.left_initial_values;
		InitialValues right = params.right_initial_values;
		double bX = params.physicalConstants.bX;

		double rhoL = left.rho;
		double pL = left.p;
		double uL = left.u;
		double vL = left.v;
		double wL = left.w;
		double bYL = left.bY;
		double bZL = left.bZ;
		for (int i = 0; i < xRes / 2; i++) {
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
		double rhoR = right.rho;
		double pR = right.p;
		double uR = right.u;
		double vR = right.v;
		double wR = right.w;
		double bYR = right.bY;
		double bZR = right.bZ;
		for (int i = xRes / 2; i < xRes; i++) {
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
		mapBuilder.put("count", String.valueOf(count)).put("total time",
				String.valueOf(totalTime));
		return mapBuilder.build();
	}

	public ImmutableMap<String, double[]> getData() {
		Builder<String, double[]> mapBuilder = ImmutableMap.builder();
		double[] u = new double[xRes];
		double[] v = new double[xRes];
		double[] w = new double[xRes];
		double[] p_star = new double[xRes];

		for (int i = 0; i < xRes; i++) {
			u[i] = roU[i] / ro[i];
			v[i] = roV[i] / ro[i];
			w[i] = roW[i] / ro[i];
			p_star[i] = getPressure(i)
					+ (bXPr[i] * bXPr[i] + bYPr[i] * bYPr[i] + bZPr[i]
							* bZPr[i]) / 8 / Math.PI;
		}
		mapBuilder.put("ro", ro);
		mapBuilder.put("u", u);
		mapBuilder.put("v", v);
		mapBuilder.put("w", w);
		mapBuilder.put("p_star", p_star);
		mapBuilder.put("bY", bY);
		mapBuilder.put("bZ", bZ);
		return mapBuilder.build();
	}

	public double[] getXCoord() {
		double[] ret = new double[xRes];
		for (int i = 0; i < ret.length; i++) {
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
		for (int i = 0; i < xRes; i++) {
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
		for (int i = 0; i < arrays.length / 2; i++) {
			System.arraycopy(arrays[2 * i], 0, arrays[2 * i + 1], 0,
					arrays[2 * i].length);
		}
	}

	private void applyStep(double timeStep, double spaceStep, double[]... var) {
		for (int k = 0; k < var.length; k++) {
			for (int i = 1; i < xRes - 1; i++) {
				var[k][i] += (flow[i - 1][k] - flow[i][k]) * timeStep
						/ spaceStep;
			}
		}
	}

	private void findPredictorFlows() {
		for (int i = 0; i < xRes - 1; i++) {
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
		for (int i = 1; i < xRes - 2; i++) {

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
			// uLArr[i] = RhoL;
			// uRArr[i] = RhoR;

			// getFlow(flow[i], RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL, GamL,
			// RhoR, UR, VR, WR, PGasR, BXR, BYR, BZR, GamR);
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
		getFlow(flow[i], RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL, GamL, RhoR,
				UR, VR, WR, PGasR, BXR, BYR, BZR, GamR);
		if (checkIsNAN(flow[i])) {
			Builder<String, Double> leftInput = ImmutableMap.builder();
			leftInput.putAll(ImmutableMap.of("RhoL", RhoL, "UL", UL, "VL", VL,
					"WL", WL, "PGasL", PGasL));
			leftInput.putAll(ImmutableMap.of("BXL", BXL, "BYL", BYL, "BZL",
					BZL, "GamL", GamL));
			Builder<String, Double> rightInput = ImmutableMap.builder();
			rightInput.putAll(ImmutableMap.of("RhoR", RhoR, "UR", UR, "VR", VR,
					"WR", WR, "PGasR", PGasR));
			rightInput.putAll(ImmutableMap.of("BXR", BXR, "BYR", BYR, "BZR",
					BZR, "GamR", GamR));

			AlgorithmError.Builder builder = AlgorithmError.builder();
			builder.put("error type", "NAN value in flow")
					.put("total time", getTotalTime()).put("count", count)
					.put("x coordinate", i * h).put("i", i)
					.put("left_input", leftInput.build())
					.put("right_input", rightInput.build())
					.put("output", flow[i]);
			throw builder.build();
		}

	}

	private void getFlow(double[] flow, double RhoL, double UL, double VL,
			double WL, double PGasL, double BXL, double BYL, double BZL,
			double GamL, double RhoR, double UR, double VR, double WR,
			double PGasR, double BXR, double BYR, double BZR, double GamR) {
		final double eps = 1.0E-12;
		final double epseig = 1.0E-12;
		final double del2 = 1.0E-30;
		double gam, pi, pi4, ccmi, ccma, b, bbb, cc, c, fuj;
		double r1, ru1, rv1, rw1, u1, v1, w1, e1, hx1, hy1, hz1, hh1, kk1;
		double r2, ru2, rv2, rw2, u2, v2, w2, e2, hx2, hy2, hz2, hh2, kk2;
		double sz1, p1, p01, f11, f12, f13, f14, f15, f16, f17, f18;
		double sz2, p2, p02, f21, f22, f23, f24, f25, f26, f27, f28;
		double ra, rua, rva, rwa, ua, va, wa, ea, hx, hy, hz, hh, kk, p0;
		double szb, sa, aas, as, aaf, af, pa, alf, als, cmas, afmc, afmas;
		double ei1, ei3, ei4, ei5, ei6, ei7, ei8, eigmax;
		double eigen1, eigen2, eigen3, eigen4, eigen5, eigen6, eigen7, eigen8;
		double hyz, sih, hyy, hzz, bet;
		double du1, du2, du3, du4, du5, du6, du7, du8, du22, du33, du44, du55;
		double sp11, sp12, sp13, sp14, sp15, sp16, sp17, sp18;
		double sp21, sp22, sp23, sp24, sp25, sp26, sp27, sp28;
		double sp31, sp32, sp33, sp34, sp35, sp36, sp37, sp38;
		double sp41, sp42, sp43, sp44, sp45, sp46, sp47, sp48;
		double sp51, sp52, sp53, sp54, sp55, sp56, sp57, sp58;
		double sp61, sp62, sp63, sp64, sp65, sp66, sp67, sp68;
		double sp71, sp72, sp73, sp74, sp75, sp76, sp77, sp78;
		double sp81, sp82, sp83, sp84, sp85, sp86, sp87, sp88;
		double so11, so12, so13, so14, so15, so16, so17, so18;
		double so21, so22, so23, so24, so25, so26, so27, so28;
		double so31, so32, so33, so34, so35, so36, so37, so38;
		double so41, so42, so43, so44, so45, so46, so47, so48;
		double so51, so52, so53, so54, so55, so56, so57, so58;
		double so61, so62, so63, so64, so65, so66, so67, so68;
		double so71, so72, so73, so74, so75, so76, so77, so78;
		double so81, so82, so83, so84, so85, so86, so87, so88;
		double qqqq1, qqqq2, qqqq3, qqqq4, qqqq5, qqqq6, qqqq7, qqqq8;
		double qqq1, qqq2, qqq3, qqq4, qqq5, qqq6, qqq7, qqq8;
		double qq1, qq2, qq3, qq4, qq5, qq6, qq7, qq8;
		// !<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!

		gam = GamL;
		bet = gam - 1.0;
		pi = 3.14159265358979323846264338327950288;
		pi4 = 0.25E0 / pi;

		r1 = RhoL;
		ru1 = UL * RhoL;
		rv1 = VL * RhoL;
		rw1 = WL * RhoL;
		p1 = PGasL;
		u1 = UL;
		v1 = VL;
		w1 = WL;

		hx1 = BXL;
		hy1 = BYL;
		hz1 = BZL;
		// hh1=(hx1**2+hy1**2+hz1**2)*pi4*0.5;
		// kk1=(u1**2+v1**2+w1**2)*r1*0.5;
		hh1 = (hx1 * hx1 + hy1 * hy1 + hz1 * hz1) * pi4 * 0.5;
		kk1 = (u1 * u1 + v1 * v1 + w1 * w1) * r1 * 0.5;
		e1 = p1 / (gam - 1.0) + kk1 + hh1;

		r2 = RhoR;
		ru2 = UR * RhoR;
		rv2 = VR * RhoR;
		rw2 = WR * RhoR;
		p2 = PGasR;
		u2 = UR;
		v2 = VR;
		w2 = WR;

		hx2 = BXR;
		hy2 = BYR;
		hz2 = BZR;
		// hh2=(hx2**2+hy2**2+hz2**2)*pi4*0.5;
		// kk2=(u2**2+v2**2+w2**2)*r2*0.5;
		hh2 = (hx2 * hx2 + hy2 * hy2 + hz2 * hz2) * pi4 * 0.5;
		kk2 = (u2 * u2 + v2 * v2 + w2 * w2) * r2 * 0.5;
		e2 = p2 / (gam - 1.0) + kk2 + hh2;
		// !--------------------------------------------------------------

		sz1 = gam * p1 / r1;
		// ! szb=sz1+(hx1**2+hy1**2+hz1**2)*pi4/r1
		// ! sa=hx1**2*pi4/r1
		// ! ss=.5d0*(szb-dsqrt(szb**2-4.d0*sz1*sa))
		// ! ss=dmax1(0.D0,ss)
		// ! sf=.5d0*(szb+dsqrt(szb**2-4.d0*sz1*sa))
		// !---------------------------------------------------------------
		// !---------------------------------------------------------------

		sz2 = gam * p2 / r2;
		// ! szb=sz2+(hx2**2+hy2**2+hz2**2)*pi4/r2
		// ! sa=hx2**2*pi4/r2
		// ! ss=.5d0*(szb-dsqrt(szb**2-4.d0*sz2*sa))
		// ! ss=dmax1(0.D0,ss)

		ccmi = Math.min(sz1, sz2);
		ccma = Math.max(sz1, sz2);
		// !---------------------------------------------------------------------
		ra = 0.5 * (r1 + r2);
		ua = 0.5 * (u1 + u2);
		va = 0.5 * (v1 + v2);
		wa = 0.5 * (w1 + w2);
		hx = 0.5 * (hx1 + hx2);
		hy = (hy1 + hy2) * .5;
		hz = (hz1 + hz2) * .5;
		p01 = p1 + hh1;
		p02 = p2 + hh2;
		p0 = (p01 + p02) * 0.5;

		if (Math.sqrt(hy * hy + hz * hz) < epseig) {
			hy = epseig * 0.707107;
			hz = epseig * 0.707107;
			// ! hy=-epseig
			// ! hz=0.d0
		}
		hh = (hx * hx + hy * hy + hz * hz) * pi4 * 0.5;
		pa = p0 - hh;
		bbb = hx * hx * pi4 / ra;
		b = Math.sqrt(bbb);
		kk = 0.5 * (ua * ua + va * va + wa * wa);
		cc = gam * pa / ra;
		if (cc < ccmi) {
			cc = ccmi;
		}
		if (cc > ccma) {
			cc = ccma;
		}
		c = Math.sqrt(cc);
		szb = cc + (hx * hx + hy * hy + hz * hz) * pi4 / ra;
		sa = Math.sqrt(szb * szb - 4.0 * cc * bbb);
		// ! c4=cc**2
		// ! c3=2*cc*bb
		// ! cc3=2*c3
		// ! b4=bb**2
		// ! write(6,*) c4,c3,cc3,b4
		// ! write(6,*) hx,hy,hz,sa,szb
		aas = 0.5 * (szb - Math.sqrt(szb * szb - 4.0 * cc * bbb));
		aas = Math.max(0.0, aas);
		aaf = 0.5 * (szb + Math.sqrt(szb * szb - 4.0 * cc * bbb));
		// !---------------------------------------------------------------
		af = Math.sqrt(aaf);
		as = Math.sqrt(aas);
		fuj = 0.3 * (Math.abs(ua) + Math.abs(va) + Math.abs(wa) + af);
		ei1 = Math.abs(ua);
		ei3 = Math.abs(ua + b);
		ei4 = Math.abs(ua - b);
		ei5 = Math.abs(ua + af);
		ei6 = Math.abs(ua - af);
		ei7 = Math.abs(ua + as);
		ei8 = Math.abs(ua - as);
		eigmax = ei1 + af;
		if (ei1 >= fuj) {
			eigen1 = ei1;
			eigen2 = ei1;
			// ! eigen1=eigmax
			// ! eigen2=eigmax
		} else {
			eigen1 = (ei1 * ei1 + fuj * fuj) / (2.0 * fuj);
			eigen2 = (ei1 * ei1 + fuj * fuj) / (2.0 * fuj);
		}
		if (ei3 >= fuj) {
			eigen3 = ei3;
			// ! eigen3=eigmax
		} else {
			eigen3 = (ei3 * ei3 + fuj * fuj) / (2. * fuj);
		}
		if (ei4 >= fuj) {
			eigen4 = ei4;
			// ! eigen4=eigmax
		} else {
			eigen4 = (ei4 * ei4 + fuj * fuj) / (2. * fuj);
		}
		if (ei5 >= fuj) {
			eigen5 = ei5;
			// ! eigen5=eigmax
		} else {
			eigen5 = (ei5 * ei5 + fuj * fuj) / (2. * fuj);
		}
		if (ei6 >= fuj) {
			eigen6 = ei6;
			// ! eigen6=eigmax
		} else {
			eigen6 = (ei6 * ei6 + fuj * fuj) / (2. * fuj);
		}
		if (ei7 >= fuj) {
			eigen7 = ei7;
			// ! eigen7=eigmax
		} else {
			eigen7 = (ei7 * ei7 + fuj * fuj) / (2. * fuj);
		}
		if (ei8 >= fuj) {
			eigen8 = ei8;
			// ! eigen8=eigmax
		} else {
			eigen8 = (ei8 * ei8 + fuj * fuj) / (2. * fuj);
		}
		// !***************************************************************
		// ! u,v,w, af>b>as>0 - speeds
		// ! -Entropy correction must be here.
		//
		// !****************************************************************
		aas = Math.min(cc, aas);
		alf = Math.sqrt(Math.abs(cc - aas) / (aaf - aas));
		als = Math.sqrt(Math.abs(aaf - cc) / (aaf - aas));
		cmas = cc - aas;
		afmc = aaf - cc;
		afmas = aaf - aas;
		hyz = Math.sqrt(hy * hy + hz * hz);
		sih = -1.0;
		if (hx >= 0.0) {
			sih = +1.0;
		}
		hyy = hy / hyz;
		hzz = hz / hyz;
		// !****************************************************************
		f11 = ru1;
		f12 = u1 * ru1 - hx1 * hx1 * pi4 + p1 + hh1;
		f13 = u1 * rv1 - hx1 * hy1 * pi4;
		f14 = u1 * rw1 - hx1 * hz1 * pi4;
		f15 = u1 * (e1 + p1 + hh1) - hx1 * (u1 * hx1 + v1 * hy1 + w1 * hz1)
				* pi4;
		f16 = 0.0;
		f17 = u1 * hy1 - hx1 * v1;
		f18 = u1 * hz1 - hx1 * w1;
		f21 = ru2;
		f22 = u2 * ru2 - hx2 * hx2 * pi4 + p2 + hh2;
		f23 = u2 * rv2 - hx2 * hy2 * pi4;
		f24 = u2 * rw2 - hx2 * hz2 * pi4;
		f25 = u2 * (e2 + p2 + hh2) - hx2 * (u2 * hx2 + v2 * hy2 + w2 * hz2)
				* pi4;
		f26 = 0.0;
		f27 = u2 * hy2 - hx2 * v2;
		f28 = u2 * hz2 - hx2 * w2;
		du1 = r1 - r2;
		du22 = ru1 - ru2;
		du33 = rv1 - rv2;
		du44 = rw1 - rw2;
		du55 = e1 - e2;
		du6 = hx1 - hx2;
		du7 = hy1 - hy2;
		du8 = hz1 - hz2;
		// !--------------------------------------------------------------------
		du2 = (-ua * du1 + du22) / ra;
		du3 = (-va * du1 + du33) / ra;
		du4 = (-wa * du1 + du44) / ra;
		du5 = -bet
				* (-kk * du1 + ua * du22 + va * du33 + wa * du44 - du55 + (hx
						* du6 + hy * du7 + hz * du8)
						* pi4);
		// !---------------------------------------------------------S-matrix
		sp15 = ra * alf;
		sp16 = ra * alf;
		sp17 = ra * als;
		sp18 = ra * als;
		sp25 = alf * af;
		sp26 = -alf * af;
		sp27 = als * as;
		sp28 = -als * as;
		sp33 = -hzz / Math.sqrt(2.0);
		sp34 = -hzz / Math.sqrt(2.0);
		sp35 = -als * as * hyy * sih;
		sp36 = als * as * hyy * sih;
		sp37 = alf * af * hyy * sih;
		sp38 = -alf * af * hyy * sih;
		sp43 = hyy / Math.sqrt(2.0);
		sp44 = hyy / Math.sqrt(2.0);
		sp45 = -als * as * hzz * sih;
		sp46 = als * as * hzz * sih;
		sp47 = alf * af * hzz * sih;
		sp48 = -alf * af * hzz * sih;
		sp55 = alf * ra * cc;
		sp56 = alf * ra * cc;
		sp57 = als * ra * cc;
		sp58 = als * ra * cc;
		sp73 = hzz * Math.sqrt(2 * pi * ra) * sih;
		sp74 = -hzz * Math.sqrt(2 * pi * ra) * sih;
		sp75 = 2. * als * Math.sqrt(pi * ra) * c * hyy;
		sp76 = 2. * als * Math.sqrt(pi * ra) * c * hyy;
		sp77 = -2. * alf * Math.sqrt(pi * ra) * c * hyy;
		sp78 = -2. * alf * Math.sqrt(pi * ra) * c * hyy;
		sp83 = -hyy * Math.sqrt(2 * pi * ra) * sih;
		sp84 = hyy * Math.sqrt(2 * pi * ra) * sih;
		sp85 = 2. * als * Math.sqrt(pi * ra) * c * hzz;
		sp86 = 2. * als * Math.sqrt(pi * ra) * c * hzz;
		sp87 = -2. * alf * Math.sqrt(pi * ra) * c * hzz;
		sp88 = -2. * alf * Math.sqrt(pi * ra) * c * hzz;
		so15 = -1. / cc;
		so33 = -hzz / Math.sqrt(2.0);
		so34 = hyy / Math.sqrt(2.0);
		so37 = .5 * hzz * sih / Math.sqrt(2 * pi * ra);
		so38 = -.5 * hyy * sih / Math.sqrt(2 * pi * ra);
		so43 = -hzz / Math.sqrt(2.0);
		so44 = hyy / Math.sqrt(2.0);
		so47 = -.5 * hzz * sih / Math.sqrt(2 * pi * ra);
		so48 = .5 * hyy * sih / Math.sqrt(2 * pi * ra);
		so52 = alf * af * .5 / cc;
		so53 = -.5 * als * as * hyy * sih / cc;
		so54 = -.5 * als * as * hzz * sih / cc;
		so55 = alf * .5 / (cc * ra);
		so57 = .25 * als * hyy / (c * Math.sqrt(pi * ra));
		so58 = .25 * als * hzz / (c * Math.sqrt(pi * ra));
		so62 = -alf * af * .5 / cc;
		so63 = .5 * als * as * hyy * sih / cc;
		so64 = .5 * als * as * hzz * sih / cc;
		so65 = alf * .5 / (cc * ra);
		so67 = .25 * als * hyy / (c * Math.sqrt(pi * ra));
		so68 = .25 * als * hzz / (c * Math.sqrt(pi * ra));
		so72 = .5 * als * as / cc;
		so73 = .5 * alf * af * hyy * sih / cc;
		so74 = .5 * alf * af * hzz * sih / cc;
		so75 = als * .5 / (ra * cc);
		so77 = -.25 * alf * hyy / (c * Math.sqrt(pi * ra));
		so78 = -.25 * alf * hzz / (c * Math.sqrt(pi * ra));
		so82 = -.5 * als * as / cc;
		so83 = -.5 * alf * af * hyy * sih / cc;
		so84 = -.5 * alf * af * hzz * sih / cc;
		so85 = als * .5 / (ra * cc);
		so87 = -.25 * alf * hyy / (c * Math.sqrt(pi * ra));
		so88 = -.25 * alf * hzz / (c * Math.sqrt(pi * ra));
		// !**************************************************************
		qqqq1 = eigen1 * (du1 + so15 * du5);
		qqqq2 = eigen2 * du6;
		qqqq3 = eigen3 * (so33 * du3 + so34 * du4 + so37 * du7 + so38 * du8);
		qqqq4 = eigen4 * (so43 * du3 + so44 * du4 + so47 * du7 + so48 * du8);
		qqqq5 = eigen5
				* (so52 * du2 + so53 * du3 + so54 * du4 + so55 * du5 + so57
						* du7 + so58 * du8);
		qqqq6 = eigen6
				* (so62 * du2 + so63 * du3 + so64 * du4 + so65 * du5 + so67
						* du7 + so68 * du8);
		qqqq7 = eigen7
				* (so72 * du2 + so73 * du3 + so74 * du4 + so75 * du5 + so77
						* du7 + so78 * du8);
		qqqq8 = eigen8
				* (so82 * du2 + so83 * du3 + so84 * du4 + so85 * du5 + so87
						* du7 + so88 * du8);
		qqq1 = qqqq1 + sp15 * qqqq5 + sp16 * qqqq6 + sp17 * qqqq7 + sp18
				* qqqq8;
		qqq2 = sp25 * qqqq5 + sp26 * qqqq6 + sp27 * qqqq7 + sp28 * qqqq8;
		qqq3 = sp33 * qqqq3 + sp34 * qqqq4 + sp35 * qqqq5 + sp36 * qqqq6 + sp37
				* qqqq7 + sp38 * qqqq8;
		qqq4 = sp43 * qqqq3 + sp44 * qqqq4 + sp45 * qqqq5 + sp46 * qqqq6 + sp47
				* qqqq7 + sp48 * qqqq8;
		qqq5 = sp55 * qqqq5 + sp56 * qqqq6 + sp57 * qqqq7 + sp58 * qqqq8;
		qqq6 = qqqq2;
		qqq7 = sp73 * qqqq3 + sp74 * qqqq4 + sp75 * qqqq5 + sp76 * qqqq6 + sp77
				* qqqq7 + sp78 * qqqq8;
		qqq8 = sp83 * qqqq3 + sp84 * qqqq4 + sp85 * qqqq5 + sp86 * qqqq6 + sp87
				* qqqq7 + sp88 * qqqq8;
		qq1 = qqq1;
		qq2 = ua * qqq1 + ra * qqq2;
		qq3 = va * qqq1 + ra * qqq3;
		qq4 = wa * qqq1 + ra * qqq4;
		qq5 = kk * qqq1 + ra * (ua * qqq2 + va * qqq3 + wa * qqq4) + qqq5 / bet
				+ pi4 * (hx * qqq6 + hy * qqq7 + hz * qqq8);
		qq6 = qqq6;
		qq7 = qqq7;
		qq8 = qqq8;
		// !---------------------------------------------------------------
		flow[0] = 0.5 * (f11 + f21 + qq1);
		flow[1] = 0.5 * (f12 + f22 + qq2);
		flow[2] = 0.5 * (f13 + f23 + qq3);
		flow[3] = 0.5 * (f14 + f24 + qq4);
		flow[4] = 0.5 * (f15 + f25 + qq5);
		flow[5] = 0.5 * (f16 + f26 + qq6);
		flow[6] = 0.5 * (f17 + f27 + qq7);
		flow[7] = 0.5 * (f18 + f28 + qq8);
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

	public void printAll(PrintStream out) {
		out.println("======================================================");
		out.println("count = " + count);
		out.println("ro");
		printArr(ro, out);
		out.println("roPr");
		printArr(roPr, out);
		out.println("roU");
		printArr(roU, out);
		out.println("roUPr");
		printArr(roUPr, out);
		out.println("roV");
		printArr(roV, out);
		out.println("roVPr");
		printArr(roVPr, out);
		out.println("roW");
		printArr(roW, out);
		out.println("roWPr");
		printArr(roWPr, out);
		out.println("e");
		printArr(e, out);
		out.println("ePr");
		printArr(ePr, out);
		out.println("bX");
		printArr(bX, out);
		out.println("bXPr");
		printArr(bXPr, out);
		out.println("bY");
		printArr(bY, out);
		out.println("bYPr");
		printArr(bYPr, out);
		out.println("bZ");
		printArr(bZ, out);
		out.println("bZPr");
		printArr(bZPr, out);
		double[] pressure = new double[xRes];
		for (int i = 0; i < xRes; i++) {
			pressure[i] = getPressure(i);
		}
		out.println("pressure");
		printArr(pressure, out);
		double[] pressurePr = new double[xRes];
		for (int i = 0; i < xRes; i++) {
			pressurePr[i] = getPressurePr(i);
		}
		out.println("pressurePr");
		printArr(pressurePr, out);
		printFlow(out);
		out.close();
	}

	private void printFlow(PrintStream out) {
		for (int i = 0; i < 8; i++) {
			out.print("[");
			out.print(flow[0][i]);
			for (int j = 0; j < flow.length; j++) {
				out.print(", ");
				out.print(flow[j][i]);
			}
			out.print("]");
			out.println();
		}
	}

	private void printArr(double[] arr, PrintStream out) {
		out.println(Arrays.toString(arr));
	}

	private boolean checkIsNAN(double[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (Double.isNaN(arr[i]))
				return true;
		}
		return false;
	}

}
