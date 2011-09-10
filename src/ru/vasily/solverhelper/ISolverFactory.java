package ru.vasily.solverhelper;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;

public interface ISolverFactory {
	MHDSolver createSolver(DataObject params);
}
