package ru.vasily.application.plotdata;

public interface PlotDataVisitor
{

    void process1D(String name, double x[], double value[]);

    void process2D(String name, double x[][], double y[][], double value[][]);
}
