package ru.vasily.solver.factory;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;

public interface IMHDSolverFactory
{
    MHDSolver createSolver(DataObject params);
}
