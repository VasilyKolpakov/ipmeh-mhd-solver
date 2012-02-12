package ru.vasily.application.plotdata;

public interface PlotData
{

    void accept(PlotDataVisitor visitor);
}
