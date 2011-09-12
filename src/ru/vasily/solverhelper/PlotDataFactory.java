package ru.vasily.solverhelper;

public final class PlotDataFactory {
	private PlotDataFactory() {
	}

	public static PlotData plots(final PlotData... plots) {
		return new PlotData() {

			@Override
			public void visit(PlotDataVisitor handler) {
				for (PlotData plot : plots)
				{
					plot.visit(handler);
				}
			}
		};
	}

	public static PlotData emptyPlot() {
		return new PlotData() {

			@Override
			public void visit(PlotDataVisitor handler) {
			}
		};
	}

	public static PlotData plot1D(final String key, final double[] x, final double[] y) {
		if (x.length != y.length)
			throw new RuntimeException("lengths of x and y arrays are not equal");
		return new PlotData() {

			@Override
			public void visit(PlotDataVisitor handler) {
				handler.handleResult1D(key, x, y);
			}
		};
	}
}
