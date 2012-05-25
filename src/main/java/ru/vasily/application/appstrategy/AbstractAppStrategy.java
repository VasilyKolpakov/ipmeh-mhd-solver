package ru.vasily.application.appstrategy;

import java.io.IOException;

import ru.vasily.core.io.FileSystem;
import ru.vasily.mydi.DIKey;
import ru.vasily.solver.CalculationResult;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.application.IResultWriter;
import ru.vasily.application.SolverFacade;

import static ru.vasily.application.ApplicationParamsConstants.DIRECTORIES_DI_KEY;
import static ru.vasily.application.ApplicationParamsConstants.OUTPUT_DIR_KEY;
import static ru.vasily.application.misc.DataObjectParser.asDataObject;
import static ru.vasily.core.io.Parsers.asString;
import static ru.vasily.core.io.Writables.asWritable;

public abstract class AbstractAppStrategy implements AppStrategy
{
    private static final String PARAMS_FILE_EXTENSION = "js";

    protected final SolverFacade solver;
    protected final IResultWriter dataWriter;

    protected final DataObjectService objService;

    protected final FileSystem fileSystem;
    private final DataObject directories;

    public AbstractAppStrategy(DataObjectService objService,
                               SolverFacade solver,
                               IResultWriter dataWriter,
                               FileSystem fileSystem,
                               @DIKey(DIRECTORIES_DI_KEY) DataObject directories)
    {
        this.objService = objService;
        this.solver = solver;
        this.dataWriter = dataWriter;
        this.fileSystem = fileSystem;
        this.directories = directories;
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
        String resultDirPath = fileSystem
                .createPath(directories.getString(OUTPUT_DIR_KEY), outputDirectoryName);
        String inputFileCopyPath = fileSystem
                .createPath(resultDirPath, "input.js");
        String inputFileContents = fileSystem.parse(path, asString());
        fileSystem.write(asWritable(inputFileContents), inputFileCopyPath);
    }

}
