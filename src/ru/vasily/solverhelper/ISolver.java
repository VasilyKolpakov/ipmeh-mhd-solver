package ru.vasily.solverhelper;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.Parameters;

public interface ISolver {
	CalculationResult solve(Parameters p);
}
