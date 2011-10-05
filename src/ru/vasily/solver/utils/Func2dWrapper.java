package ru.vasily.solver.utils;

import ru.vasily.solver.initialcond.Init2dFunction;

public class Func2dWrapper implements Init2dFunction
{
	private final Init2dFunction function;
	private final double xLength;
	private final double yLength;

	public Func2dWrapper(double xLength, double yLength, Init2dFunction function)
	{
		this.xLength = xLength;
		this.yLength = yLength;
		this.function = function;
	}

	@Override
	public void apply(double[] arr, double x, double y)
	{
		function.apply(arr, x * xLength, y * yLength);
	}
}
