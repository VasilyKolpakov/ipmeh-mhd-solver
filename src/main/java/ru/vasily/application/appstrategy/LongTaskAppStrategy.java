package ru.vasily.application.appstrategy;

import ru.vasily.core.io.FileSystem;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;
import ru.vasily.application.SolverFacade.TimeLimitedIterativeSolver;

import java.io.IOException;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
            numberOfIterations = 10 * (int) max(1, min((double) numberOfIterations / loopTime, 1000));
        }
    }

    private double timeInMinutesFrom(long loopStartTime)
    {
        return (double) (System.currentTimeMillis() - loopStartTime) / 60000.0;
    }

}
