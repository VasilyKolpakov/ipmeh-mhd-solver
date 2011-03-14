package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;

import ru.vasily.dataobjs.CalculationResult;

public interface ITemplateManager {
	void createLayoutFiles(File templateDir, CalculationResult resultData, File outputDir) throws IOException;
}
