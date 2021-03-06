package ru.vasily.solver;

import ru.vasily.core.io.Writable;
import ru.vasily.application.plotdata.PlotData;

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
