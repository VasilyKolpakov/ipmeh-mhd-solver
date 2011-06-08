package ru.vasily.solverhelper;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;

public interface ISolver {
	CalculationResult solve(DataObject p);

	IterativeSolver getSolver(DataObject p);

	public interface IterativeSolver {
		CalculationResult next(int iterations);
	}
}
