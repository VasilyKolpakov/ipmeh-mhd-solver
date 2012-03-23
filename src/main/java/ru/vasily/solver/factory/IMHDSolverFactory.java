package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;

public interface IMHDSolverFactory
{
    public static String CALCULATION_CONSTANTS = "calculationConstants";
    public static String PHYSICAL_CONSTANTS = "physicalConstants";

    MHDSolver createSolver(DataObject params);
}
