package ru.vasily.solverhelper;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.io.FileSystem;
import ru.vasily.core.templates.FileTemplater;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.mydi.DIKey;
import ru.vasily.solverhelper.plotdata.PlotDataVisitor;

import java.io.IOException;
import java.util.Map;

import static ru.vasily.core.collection.Range.range;
import static ru.vasily.solverhelper.ApplicationParamsConstants.*;

public class TemplatingResultWriter implements IResultWriter
{

    private final FileTemplater fileTemplater;
    private final FileSystem fileSystem;
    private final DataObject directories;

    public TemplatingResultWriter(FileTemplater fileTemplater, FileSystem fileSystem,
                                  @DIKey(DIRECTORIES_DI_KEY) DataObject directories)
    {
        this.fileTemplater = fileTemplater;
        this.fileSystem = fileSystem;
        this.directories = directories;
    }

    @Override
    public void createResultDir(String resultDirectoryName, CalculationResult result) throws IOException
    {
        String resultDirPath = fileSystem.createPath(directories.getString(OUTPUT_DIR_KEY), resultDirectoryName);
        result.data.accept(new PlotDataWriter(resultDirPath));
        fileSystem.write(result.log, fileSystem.createPath(resultDirPath, "log.js"));
    }

    private class PlotDataWriter implements PlotDataVisitor
    {
        private final String resultDirPath;

        private PlotDataWriter(String resultDirPath)
        {
            this.resultDirPath = resultDirPath;
        }

        @Override
        public void process1D(String name, double[] x, double[] value)
        {
            Map<String, Object> context = ImmutableMap.<String, Object>builder()
                    .put("valueName", name)
                    .put("xs", x)
                    .put("value", value)
                    .put("xRes", x.length)
                    .build();
            createResultFiles(context, "1D");
        }

        @Override
        public void process2D(String name, double[][] x, double[][] y, double[][] value)
        {
            Map<String, Object> context = ImmutableMap.<String, Object>builder()
                    .put("valueName", name)
                    .put("xs", x).put("ys", y)
                    .put("value", value)
                    .put("xRes", x.length).put("yRes", x[0].length)
                    .build();
            createResultFiles(context, "2D");
        }

        private void createResultFiles(Map<String, Object> context, String s)
        {
            String templatePath = fileSystem.createPath(directories.getString(TEMPLATE_DIR_KEY), s);
            fileTemplater.renderFile(templatePath, context, resultDirPath);
        }
    }
}
