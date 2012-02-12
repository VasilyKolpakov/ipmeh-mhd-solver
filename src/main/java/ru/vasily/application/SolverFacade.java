package ru.vasily.application;

import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObject;

public interface SolverFacade
{
    CalculationResult solve(DataObject p);

    IterativeSolver getSolver(DataObject p);

    TimeLimitedIterativeSolver getTimeLimitedSolver(DataObject p);

    public interface IterativeSolver
    {
        CalculationResult next(int iterations);
    }

    public interface TimeLimitedIterativeSolver extends IterativeSolver
    {
        boolean isTimeLimitReached();
    }

}
