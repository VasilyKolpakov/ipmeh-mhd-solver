package ru.vasily.dataobjs;

public class CalculationResult {
	private Iterable<ArrayDataObj> data;
	private final String log;

	public CalculationResult(Iterable<ArrayDataObj> data,String log) {
		super();
		this.data = data;
		this.log = log;
	}

	public Iterable<ArrayDataObj> getData() {
		return data;
	}

	public String getLog() {
		return log;
	}

}
