package ru.vasily.solverhelper;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.Parameters;

public interface ISolver
{
	CalculationResult solve(Parameters p);

	IterativeSolver getSolver(Parameters p);

	public interface IterativeSolver
	{
		CalculationResult next(int iterations);
	}
}
