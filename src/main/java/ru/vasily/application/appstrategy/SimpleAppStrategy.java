package ru.vasily.application.appstrategy;

import java.io.IOException;

import ru.vasily.core.io.FileSystem;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;

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
        CalculationResult result = solver.solve(parseDataObject(input));
        System.out.println("input data = " + fileSystem.getFileName(input));
        System.out.println("time = " + (System.currentTimeMillis() - time));
        writeResult(input, result);
    }
}
