package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataFile;
import ru.vasily.solverhelper.ITemplateManager.Templater;

public class ResultWriter implements IResultWriter {
	private final ITemplateManager templateManager;
	private final FileSystem fileSystem;

	public ResultWriter(ITemplateManager templateManager, FileSystem fileSystem) {
		this.templateManager = templateManager;
		this.fileSystem = fileSystem;
	}

	@Override
	public void createResultDir(File path, CalculationResult result,
			File templateDir) throws IOException {
		createResultDir(path, result);
		createLayoutFiles(path, templateDir, result);
		fileSystem.write(result.log, new File(path, "log.txt"), Charsets.UTF_8);
	}

	private void createLayoutFiles(File path, File templateDir, CalculationResult result) {
		Templater templater = templateManager.loadTemplate(templateDir, path);
		result.data.visit(new TemplateWritingVisitor(templater));
	}

	@Override
	public void createResultDir(final File path, CalculationResult result) {
		if (!fileSystem.exists(path))
		{
			fileSystem.mkdir(path);
		}
		PlotDataVisitor visitor = new PlotDataVisitor() {

			@Override
			public void handleResult1D(String name, double[] x, double[] y) {
				writeData(path, name, x, y);
			}
		};
		result.data.visit(visitor);
	}

	private void writeData(File path, String name, double[] x, double[] y) {
		Map<String, double[]> dataMap = ImmutableMap.of(name, y);
		File outPath = new File(path, name + ".dat");
		try
		{
			fileSystem.write(
					DataFile.createFile(name, x, dataMap),
					outPath
					);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
