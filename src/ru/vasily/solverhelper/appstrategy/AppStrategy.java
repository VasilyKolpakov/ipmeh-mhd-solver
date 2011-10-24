package ru.vasily.solverhelper.appstrategy;

import java.io.File;
import java.io.IOException;

public interface AppStrategy
{
	void processInputFile(File inputFile, File templateDir, File outputDir) throws IOException;
}
