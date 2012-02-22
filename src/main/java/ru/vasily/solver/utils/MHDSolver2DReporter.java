package ru.vasily.solver.utils;

import ru.vasily.application.plotdata.PlotData;

public interface MHDSolver2DReporter
{
    PlotData report(double[][] x, double[][] y, double[][][] val, double[][][] up_down_flow, double[][][] left_right_flow, double gamma);
}
