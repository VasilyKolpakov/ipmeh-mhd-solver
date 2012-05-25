package ru.vasily.application.appstrategy;

import java.io.IOException;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.io.FileSystem;
import ru.vasily.mydi.DIKey;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;

import static ru.vasily.application.ApplicationParamsConstants.DIRECTORIES_DI_KEY;

public class SimpleAppStrategy extends AbstractAppStrategy
{

    public SimpleAppStrategy(DataObjectService objService, SolverFacade solver,
                             IResultWriter dataWriter, FileSystem fileSystem,
                             @DIKey(DIRECTORIES_DI_KEY) DataObject directories)
    {
        super(objService, solver, dataWriter, fileSystem, directories);
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
