package ru.vasily.solver;

public interface RiemannSolver2D {

	void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR,
			double cos_alfa, double sin_alfa);
}
