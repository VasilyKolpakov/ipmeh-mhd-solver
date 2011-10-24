package ru.vasily.solverhelper.appstrategy;

import java.io.File;
import java.io.IOException;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;
import ru.vasily.solverhelper.SolverFacade.TimeLimitedIterativeSolver;

public class LongTaskAppStrategy extends AbstractAppStrategy
{

	public LongTaskAppStrategy(DataObjectService objService, SolverFacade solver,
			IResultWriter dataWriter, FileSystem fileSystem)
	{
		super(objService, solver, dataWriter, fileSystem);
	}

	@Override
	public void processInputFile(File inputFile, File templateDir, File outputDir)
			throws IOException
	{
		int numberOfIterations = 10;
		long startTime = System.currentTimeMillis();

		System.out.println("input data = " + inputFile.getName());
		TimeLimitedIterativeSolver timeLimitedSolver = solver
				.getTimeLimitedSolver(parseParams(inputFile));
		while (!timeLimitedSolver.isTimeLimitReached())
		{
			long loopStartTime = System.currentTimeMillis();
			CalculationResult result = timeLimitedSolver.next(numberOfIterations);
			double loopTime = timeInMinutesFrom(loopStartTime);
			System.out.println("loop time (minutes) = " + loopTime);
			writeResult(outputDir, templateDir, inputFile, result);
			double totalTime = timeInMinutesFrom(startTime);
			System.out.println("input data = " + inputFile.getName());
			System.out.println("total time (minutes) = " + totalTime);
			System.out.println("log data = " + result.log);
			numberOfIterations = (int) Math.max(1,
					Math.min(10 * numberOfIterations / loopTime, 1000));
		}
	}

	private double timeInMinutesFrom(long loopStartTime)
	{
		return (double) (System.currentTimeMillis() - loopStartTime) / 60000.0;
	}

}
