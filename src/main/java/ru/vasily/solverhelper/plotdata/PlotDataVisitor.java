package ru.vasily.solverhelper.plotdata;

public interface PlotDataVisitor
{

    void process1D(String name, double x[], double y[]);

    void process2D(String name, double x[][], double y[][], double val[][]);
}
