package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;

import ru.vasily.dataobjs.Parameters;

public interface IParamsLoader {
	Parameters getParams(File file) throws IOException;
}
