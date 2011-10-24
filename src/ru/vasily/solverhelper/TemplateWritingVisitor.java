package ru.vasily.solverhelper;

import ru.vasily.solverhelper.ITemplateManager.Templater;
import ru.vasily.solverhelper.plotdata.PlotDataVisitor;

import com.google.common.collect.ImmutableMap;

import static ru.vasily.solverhelper.misc.ArrayUtils.*;

public class TemplateWritingVisitor implements PlotDataVisitor {

	private final Templater templater;

	public TemplateWritingVisitor(Templater templater) {
		this.templater = templater;
	}

	@Override
	public void process1D(String name, double[] x, double[] y) {
		ImmutableMap<String, String> params = ImmutableMap.<String, String> builder()
				.put("value_name", name)
				.put("min_x", String.valueOf(min(x)))
				.put("max_x", String.valueOf(max(x)))
				.put("min_y", String.valueOf(min(y)))
				.put("max_y", String.valueOf(max(y)))
				.build();
		templater.writeLayout("1D", params);
	}

	@Override
	public void process2D(String name, double[][] x, double[][] y, double[][] val)
	{
		ImmutableMap<String, String> params = ImmutableMap.<String, String> builder()
				.put("value_name", name)
				.put("min_x", String.valueOf(min(x)))
				.put("max_x", String.valueOf(max(x)))
				.put("min_y", String.valueOf(min(y)))
				.put("max_y", String.valueOf(max(y)))
				.put("min_value", String.valueOf(min(val)))
				.put("max_value", String.valueOf(max(val)))
				.build();
		templater.writeLayout("2D", params);
	}
}
