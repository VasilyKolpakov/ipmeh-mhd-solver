package ru.vasily.solverhelper;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;

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
