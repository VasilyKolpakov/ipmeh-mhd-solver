package ru.vasily.solverhelper.appstrategy;

import java.io.IOException;

import ru.vasily.core.io.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.SolverFacade;
import ru.vasily.solverhelper.misc.DataObjectParser;

import static ru.vasily.solverhelper.misc.DataObjectParser.asDataObject;

public abstract class AbstractAppStrategy implements AppStrategy
{
    private static final String PARAMS_FILE_EXTENSION = "js";

    protected final SolverFacade solver;
    protected final IResultWriter dataWriter;

    protected final DataObjectService objService;

    protected final FileSystem fileSystem;

    public AbstractAppStrategy(DataObjectService objService, SolverFacade solver,
                               IResultWriter dataWriter, FileSystem fileSystem)
    {
        this.objService = objService;
        this.solver = solver;
        this.dataWriter = dataWriter;
        this.fileSystem = fileSystem;
    }

    protected final DataObject parseDataObject(String path) throws IOException
    {
        return fileSystem.parse(path, asDataObject(objService));
    }

    protected final void writeResult(String path,
                                     CalculationResult result) throws IOException
    {
        String outputDirectoryName = fileSystem.getFileName(path).replace("." + PARAMS_FILE_EXTENSION, "");
        dataWriter.createResultDir(outputDirectoryName, result);
    }

}
