package ru.vasily.solver.utils;

import ru.vasily.solverhelper.plotdata.PlotData;

public interface MHDSolver2DReporter
{
	PlotData report(double[][] x, double[][] y, double[][][] val,
			double[][] divB, double gamma);
}
