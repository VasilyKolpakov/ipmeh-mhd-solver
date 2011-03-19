package ru.vasily.dataobjs;

public class CalculationResult {
	private Iterable<DataObj> data;
	private final String log;

	public CalculationResult(Iterable<DataObj> data,String log) {
		super();
		this.data = data;
		this.log = log;
	}

	public Iterable<DataObj> getData() {
		return data;
	}

	public String getLog() {
		return log;
	}

}
