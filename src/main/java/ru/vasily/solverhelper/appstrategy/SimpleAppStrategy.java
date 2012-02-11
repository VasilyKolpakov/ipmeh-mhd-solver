package ru.vasily.solverhelper.appstrategy;

import java.io.IOException;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;

public class SimpleAppStrategy extends AbstractAppStrategy
{

    public SimpleAppStrategy(DataObjectService objService, SolverFacade solver,
                             IResultWriter dataWriter, FileSystem fileSystem)
    {
        super(objService, solver, dataWriter, fileSystem);
    }

    @Override
    public void processInputFile(String input) throws IOException
    {
        long time = System.currentTimeMillis();
        CalculationResult result = solver.solve(parseParams(input));
        System.out.println("input data = " + fileSystem.getFileName(input));
        System.out.println("time = " + (System.currentTimeMillis() - time));
        writeResult(input, result);
    }
}
