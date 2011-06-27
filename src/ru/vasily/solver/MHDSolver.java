package ru.vasily.solver;

import java.util.Map;

public interface MHDSolver {

	void nextTimeStep();

	double getTotalTime();

	Map<String, Object> getLogData();

	Map<String, double[]> getData(); // TODO refactor

	double[] getXCoord(); // TODO refactor
}
