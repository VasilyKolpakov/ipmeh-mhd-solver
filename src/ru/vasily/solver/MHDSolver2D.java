package ru.vasily.solver;

import ru.vasily.dataobjs.DataObject;

public class MHDSolver2D {
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

	public MHDSolver2D(DataObject parameters) {
		DataObject calculationConstants = parameters.getObj("calculationConstants");
		DataObject physicalConstants = parameters.getObj("physicalConstants");

		xRes = calculationConstants.getInt("xRes");
		yRes = calculationConstants.getInt("yRes");
		gamma = physicalConstants.getDouble("gamma");
		h = physicalConstants.getDouble("xLenght") / xRes;
		omega = calculationConstants.getDouble("omega");
		nu = calculationConstants.getDouble("nu");
		CFL = calculationConstants.getDouble("CFL");
		riemannSolver = new RoeSolverByKryukov();
		consVal = new double[8][yRes][xRes];
	}

	private void calculateFlow(double[][][] left_right_flow,
			double[][][] up_down_flow, double[][][] consVal) {
		
	}
}
