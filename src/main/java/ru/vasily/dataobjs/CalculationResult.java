package ru.vasily.dataobjs;

import ru.vasily.core.Writable;
import ru.vasily.solverhelper.plotdata.PlotData;

public class CalculationResult
{
    public final Writable log;
    public final PlotData data;
    public final boolean success;

    public CalculationResult(PlotData data, Writable log, boolean success)
    {
        super();
        this.data = data;
        this.log = log;
        this.success = success;
    }
}
