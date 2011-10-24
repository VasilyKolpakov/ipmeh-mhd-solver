package ru.vasily.solverhelper.appstrategy;

import java.io.File;
import java.io.IOException;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;
import ru.vasily.solverhelper.SolverFacade.IterativeSolver;

public class IterativeAppStrategy extends AbstractAppStrategy
{

	public IterativeAppStrategy(DataObjectService objService, SolverFacade solver,
			IResultWriter dataWriter, FileSystem fileSystem)
	{
		super(objService, solver, dataWriter, fileSystem);
	}

	@Override
	public void processInputFile(File inputFile, File templateDir, File outputDir)
			throws IOException
	{
		IterativeSolver iterativeSolver = solver.getSolver(parseParams(inputFile));
		{
			CalculationResult result = iterativeSolver.next(0);
			System.out.println("input data = " + inputFile.getName());
			System.out.println(result.log);
			writeResult(outputDir, templateDir, inputFile, result);
		}
		while (true)
		{
			String input = System.console().readLine(
					"write number of iterations or \'skip\':\n");
			if ("skip".equals(input))
				break;
			int n = parseInt(input);
			long time = System.currentTimeMillis();
			CalculationResult result = iterativeSolver.next(n);
			System.out.println("time = " + (System.currentTimeMillis() - time));
			System.out.println(result.log);
			writeResult(outputDir, templateDir, inputFile, result);
		}
	}

	private int parseInt(String input)
	{

		int result = 0;
		try
		{
			result = Integer.parseInt(input);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
		return result < 0 ? 0 : result;
	}

}
