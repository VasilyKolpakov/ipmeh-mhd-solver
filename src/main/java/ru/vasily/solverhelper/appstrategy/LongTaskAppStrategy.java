package ru.vasily.solverhelper.appstrategy;

import ru.vasily.core.io.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;
import ru.vasily.solverhelper.SolverFacade.TimeLimitedIterativeSolver;

import java.io.IOException;

public class LongTaskAppStrategy extends AbstractAppStrategy
{

    public LongTaskAppStrategy(DataObjectService objService, SolverFacade solver,
                               IResultWriter dataWriter, FileSystem fileSystem)
    {
        super(objService, solver, dataWriter, fileSystem);
    }

    @Override
    public void processInputFile(String inputFile)
            throws IOException
    {
        int numberOfIterations = 10;
        long startTime = System.currentTimeMillis();

        System.out.println("input data = " + fileSystem.getFileName(inputFile));
        TimeLimitedIterativeSolver timeLimitedSolver = solver
                .getTimeLimitedSolver(parseDataObject(inputFile));
        while (!timeLimitedSolver.isTimeLimitReached())
        {
            long loopStartTime = System.currentTimeMillis();
            CalculationResult result = timeLimitedSolver.next(numberOfIterations);
            double loopTime = timeInMinutesFrom(loopStartTime);
            System.out.println("loop time (minutes) = " + loopTime);
            writeResult(inputFile, result);
            double totalTime = timeInMinutesFrom(startTime);
            System.out.println("input data = " + fileSystem.getFileName(inputFile));
            System.out.println("total time (minutes) = " + totalTime);
            System.out.println("log data = " + result.log);
            numberOfIterations = 5 * (int) Math.max(1,
                                                    Math.min(1 * numberOfIterations / loopTime, 1000));
        }
    }

    private double timeInMinutesFrom(long loopStartTime)
    {
        return (double) (System.currentTimeMillis() - loopStartTime) / 60000.0;
    }

}
