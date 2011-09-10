package ru.vasily.solverhelper;

import ru.vasily.solverhelper.ITemplateManager.Templater;

import com.google.common.collect.ImmutableMap;

import static ru.vasily.solverhelper.misc.ArrayUtils.*;

public class TemplateWritingVisitor implements PlotDataVisitor {

	private final Templater templater;

	public TemplateWritingVisitor(Templater templater) {
		this.templater = templater;
	}

	@Override
	public void handleResult1D(String name, double[] x, double[] y) {
		ImmutableMap<String, String> params = ImmutableMap.<String, String> builder()
				.put("value_name", name)
				.put("min_x", String.valueOf(min(x)))
				.put("max_x", String.valueOf(max(x)))
				.put("min_y", String.valueOf(min(y)))
				.put("max_y", String.valueOf(max(y)))
				.build();
		templater.writeLayout("1D", params);
	}
}
