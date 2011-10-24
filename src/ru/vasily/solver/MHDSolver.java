package ru.vasily.solver;

import java.util.Map;

import ru.vasily.solverhelper.plotdata.PlotData;

public interface MHDSolver {

	void nextTimeStep();

	double getTotalTime();

	Map<String, Object> getLogData();

	PlotData getData();
}
