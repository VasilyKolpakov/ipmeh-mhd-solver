package ru.vasily.solverhelper.tecplot;

import java.io.File;
import java.io.IOException;

public interface ITecplotManager {
	void runMacro(File macro) throws IOException;

	void runMacro(Iterable<File> macro) throws IOException;
}
