package ru.vasily.solver;

import ru.vasily.dataobjs.DataObject;

public interface IMHDSolverFactory {
	MHDSolver createSolver(DataObject params);
}
