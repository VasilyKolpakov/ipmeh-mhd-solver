package ru.vasily.application.appstrategy;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.io.FileSystem;
import ru.vasily.mydi.DIKey;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;
import ru.vasily.application.SolverFacade.TimeLimitedIterativeSolver;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.vasily.application.ApplicationParamsConstants.DIRECTORIES_DI_KEY;

public class LongTaskAppStrategy extends AbstractAppStrategy
{

    private volatile boolean resultWritingRequired = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        @Override
        public Thread newThread(Runnable r)
        {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });

    public LongTaskAppStrategy(DataObjectService objService, SolverFacade solver,
                               IResultWriter dataWriter, FileSystem fileSystem,
                               @DIKey(DIRECTORIES_DI_KEY) DataObject directories)
    {
        super(objService, solver, dataWriter, fileSystem, directories);
    }

    @Override
    public void processInputFile(String inputFile)
            throws IOException
    {
        int numberOfIterations = 10;
        long startTime = System.currentTimeMillis();
        Future<?> future = executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    String input = System.console().readLine(
                            "write any string get the results");
                    resultWritingRequired = true;
                }
            }
        });
        System.out.println("input data = " + fileSystem.getFileName(inputFile));
        TimeLimitedIterativeSolver timeLimitedSolver = solver
                .getTimeLimitedSolver(parseDataObject(inputFile));
        while (!timeLimitedSolver.isTimeLimitReached())
        {
            long loopStartTime = System.currentTimeMillis();
            CalculationResult result = timeLimitedSolver.next(numberOfIterations);
            double loopTime = timeInMinutesFrom(loopStartTime);
            System.out.println("loop time (minutes) = " + loopTime);
            if (resultWritingRequired || timeLimitedSolver.isTimeLimitReached())
            {
                resultWritingRequired = false;
                writeResult(inputFile, result);
                System.out.println("LongTaskAppStrategy.processInputFile writing files");
            }
            double totalTime = timeInMinutesFrom(startTime);
            System.out.println("input data = " + fileSystem.getFileName(inputFile));
            System.out.println("total time (minutes) = " + totalTime);
            System.out.println("log data = " + result.log);
            numberOfIterations = 10 * (int) max(1, min((double) numberOfIterations / loopTime, 1000));
        }
        future.cancel(true);
    }

    private double timeInMinutesFrom(long loopStartTime)
    {
        return (double) (System.currentTimeMillis() - loopStartTime) / 60000.0;
    }

}
