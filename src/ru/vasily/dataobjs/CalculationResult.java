package ru.vasily.dataobjs;

import ru.vasily.solverhelper.PlotData;

public class CalculationResult {
	public final String log;
	public final PlotData data;

	public CalculationResult(PlotData data, String log) {
		super();
		this.data = data;
		this.log = log;
	}
}
