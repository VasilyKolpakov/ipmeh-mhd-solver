package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ru.vasily.dataobjs.CalculationResult;

public interface ITemplateManager {
	public void createLayoutFiles(File templateDir, Iterable<Map<String,String>> data, File outputDir)
			throws IOException;
}
