package ru.vasily.solverhelper;

import java.util.Map;

import ru.vasily.solverhelper.misc.Callback;

public class ParameterRetrievingResultHandler implements ResultHandler {

	private final Callback<Map<String, String>> paramsHandler;

	public ParameterRetrievingResultHandler(Callback<Map<String, String>> paramsHandler) {
		this.paramsHandler = paramsHandler;
	}

	@Override
	public void handleResult1D(String name, double[] x, double[] y) {
	}

}
