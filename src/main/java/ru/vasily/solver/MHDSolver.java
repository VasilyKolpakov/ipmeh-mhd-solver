package ru.vasily.solver;

import java.util.Map;

import ru.vasily.application.plotdata.PlotData;

public interface MHDSolver
{

    void nextTimeSteps(int steps, double timeLimit);

    double getTotalTime();

    Map<String, Object> getLogData();

    PlotData getData();
}
