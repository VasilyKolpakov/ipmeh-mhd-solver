package ru.vasily.dataobjs;

import ru.vasily.solverhelper.plotdata.PlotData;

public class CalculationResult
{
    public final String log;
    public final PlotData data;
    public final boolean success;

    public CalculationResult(PlotData data, String log, boolean success)
    {
        super();
        this.data = data;
        this.log = log;
        this.success = success;
    }
}
