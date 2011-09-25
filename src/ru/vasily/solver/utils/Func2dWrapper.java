package ru.vasily.solver.utils;

public class Func2dWrapper implements ArrayInit2dFunction
{
	private final ArrayInit2dFunction function;
	private final double xLength;
	private final double yLength;

	public Func2dWrapper(double xLength, double yLength, ArrayInit2dFunction function)
	{
		this.xLength = xLength;
		this.yLength = yLength;
		this.function = function;
	}

	@Override
	public void init(double[] arr, double x, double y)
	{
		function.init(arr, x * xLength, y * yLength);
	}
}
