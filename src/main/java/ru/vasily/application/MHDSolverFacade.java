package ru.vasily.application;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.*;
import ru.vasily.solver.factory.IMHDSolverFactory;
import ru.vasily.application.misc.ISerializer;
import ru.vasily.application.plotdata.PlotData;

import static ru.vasily.solver.factory.IMHDSolverFactory.*;

public class MHDSolverFacade implements SolverFacade
{

    private final ISerializer serializer;
    private final IMHDSolverFactory solverFactory;

    public MHDSolverFacade(ISerializer serializer, IMHDSolverFactory solverFactory)
    {
        this.serializer = serializer;
        this.solverFactory = solverFactory;
    }

    @Override
    public CalculationResult solve(DataObject p)
    {
        MHDSolver solver = solverFactory.createSolver(p);
        return calculate(solver,
                         iterateWithTimeLimit(solver, p.getObj(PHYSICAL_CONSTANTS).getDouble("totalTime")));
    }

    private CalculationResult calculate(MHDSolver solver, Runnable calcTask)
    {
        System.out.println("MHDSolverFacade.calculate");
        try
        {
            calcTask.run();
        }
        catch (AlgorithmError err)
        {
            StringBuilder sb = new StringBuilder();
            Map<String, Object> errorLog = err.errorLog();
            Map<String, Object> log = ImmutableMap.<String, Object>builder()
                                                  .put("error log", errorLog).put("solver log", solver.getLogData())
                                                  .build();
            ;
            CalculationResult calculationResult =
                    new CalculationResult(
                            solver.getData(),
                            serializer.asWritable(log),
                            false
                    );
            return calculationResult;
        }
        Map<String, Object> logData = solver.getLogData();
        CalculationResult calculationResult =
                createSuccessCalculationResult(solver.getData(), logData);
        return calculationResult;
    }

    @Override
    public IterativeSolver getSolver(final DataObject p)
    {
        return new IterativeSolver()
        {
            private MHDSolver solver = solverFactory.createSolver(p);

            @Override
            public CalculationResult next(int iterations)
            {
                return calculate(solver,
                                 iterateWithCountLimit(solver, iterations));
            }

        };
    }

    private CalculationResult createSuccessCalculationResult(
            PlotData plotData,
            Map<String, Object> logData)
    {
        CalculationResult calculationResult = new CalculationResult(
                plotData,
                serializer.asWritable(logData),
                true
        );
        return calculationResult;
    }

    private static Runnable iterateWithCountAndTimeLimit(final MHDSolver solver,
                                                         final int limit, final double totalTime)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                solver.nextTimeSteps(limit, totalTime);
            }
        };
    }

    private static Runnable iterateWithCountLimit(final MHDSolver solver,
                                                  final int limit)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                solver.nextTimeSteps(limit, Double.MAX_VALUE);
            }
        };
    }

    private static Runnable iterateWithTimeLimit(final MHDSolver solver,
                                                 final double totalTime)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                solver.nextTimeSteps(Integer.MAX_VALUE, totalTime);
            }
        };
    }

    @Override
    public TimeLimitedIterativeSolver getTimeLimitedSolver(final DataObject p)
    {
        final double totalTime = p.getObj(PHYSICAL_CONSTANTS).getDouble("totalTime");
        return new TimeLimitedIterativeSolver()
        {
            private MHDSolver solver = solverFactory.createSolver(p);
            private boolean currentResultSucsess = true;

            @Override
            public CalculationResult next(int iterations)
            {
                final CalculationResult currentResult = calculate(
                        solver,
                        iterateWithCountAndTimeLimit(solver, iterations,
                                                     totalTime));
                currentResultSucsess = currentResult.success;
                return currentResult;
            }

            @Override
            public boolean isTimeLimitReached()
            {
                return solver.getTotalTime() >= totalTime || (!currentResultSucsess);
            }

        };
    }
}
