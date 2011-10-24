package ru.vasily.dataobjs;

import ru.vasily.solverhelper.plotdata.PlotData;

public class CalculationResult {
	public final String log;
	public final PlotData data;
	public final boolean sucsess;

	public CalculationResult(PlotData data, String log,boolean sucsess) {
		super();
		this.data = data;
		this.log = log;
		this.sucsess = sucsess;
	}
}
