package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;

import ru.vasily.dataobjs.CalculationResult;

public interface IResultWriter {
	void createResultDir(File path, CalculationResult result) throws IOException;

	void createResultDir(File path, CalculationResult result, File templateDir)
			throws IOException;
}
