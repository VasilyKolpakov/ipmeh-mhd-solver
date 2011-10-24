package ru.vasily.solverhelper.appstrategy;

import java.io.File;
import java.io.IOException;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;
import ru.vasily.solverhelper.misc.DataObjectParser;

public abstract class AbstractAppStrategy implements AppStrategy
{
	private static final String PARAMS_FILE_EXTENSION = "js";

	protected final SolverFacade solver;
	protected final IResultWriter dataWriter;

	protected final DataObjectService objService;

	protected final FileSystem fileSystem;

	public AbstractAppStrategy(DataObjectService objService, SolverFacade solver,
			IResultWriter dataWriter, FileSystem fileSystem)
	{
		this.objService = objService;
		this.solver = solver;
		this.dataWriter = dataWriter;
		this.fileSystem = fileSystem;
	}

	protected final DataObject parseParams(File path) throws IOException
	{
		return fileSystem.parse(new DataObjectParser(objService), path);
	}

	protected final void writeResult(final File output, File template, File path,
			CalculationResult result) throws IOException
	{
		dataWriter.createResultDir(new File(output, path.getName().substring(0,
				path.getName().length() - PARAMS_FILE_EXTENSION.length()
						- 1)),
				result, template);
	}

}
