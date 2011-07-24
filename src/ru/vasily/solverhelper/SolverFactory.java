package ru.vasily.solverhelper;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solver.MHDSolver2D;

public class SolverFactory implements ISolverFactory {

	@Override
	public MHDSolver createSolver(DataObject params) {
		return new MHDSolver2D(params);
	}

}
