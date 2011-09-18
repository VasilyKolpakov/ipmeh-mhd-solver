package ru.vasily.solverhelper;

public final class PlotDataFactory
{
	private PlotDataFactory()
	{
	}

	public static PlotData plots(final PlotData... plots)
	{
		return new PlotData()
		{

			@Override
			public void accept(PlotDataVisitor handler)
			{
				for (PlotData plot : plots)
				{
					plot.accept(handler);
				}
			}
		};
	}

	public static PlotData emptyPlot()
	{
		return new PlotData()
		{

			@Override
			public void accept(PlotDataVisitor handler)
			{
			}
		};
	}

	public static PlotData plot1D(final String key, final double[] x, final double[] y)
	{
		if (x.length != y.length)
			throw new RuntimeException("lengths of x and y arrays are not equal");
		return new PlotData()
		{
			@Override
			public void accept(PlotDataVisitor handler)
			{
				handler.process1D(key, x, y);
			}
		};
	}

	public static PlotData plot2D(final String key, final double[][] x, final double[][] y, final double[][] val)
	{
		return new PlotData()
		{
			@Override
			public void accept(PlotDataVisitor handler)
			{
				handler.process2D(key, x, y, val);
			}
		};
	}
}
