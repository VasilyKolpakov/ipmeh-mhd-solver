package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataFile;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.mydi.DIKey;
import ru.vasily.solverhelper.ITemplateManager.Templater;
import ru.vasily.solverhelper.plotdata.PlotDataVisitor;
import ru.vasily.solverhelper.tecplot.DatFile2d;

import static ru.vasily.solverhelper.ApplicationParamsConstants.*;

public class ResultWriter implements IResultWriter
{
    private final ITemplateManager templateManager;
    private final FileSystem fileSystem;
    private DataObject directories;

    public ResultWriter(ITemplateManager templateManager, FileSystem fileSystem, @DIKey(DIRECTORIES_DI_KEY) DataObject directories)
    {
        this.templateManager = templateManager;
        this.fileSystem = fileSystem;
        this.directories = directories;
    }

    @Override
    public void createResultDir(String directoryName, CalculationResult result)
            throws IOException
    {
        File path = new File(directories.getString(OUTPUT_DIR_KEY), directoryName);
        createResultDir(path, result);
        File templateDir = new File(directories.getString(TEMPLATE_DIR_KEY));
        createLayoutFiles(path, templateDir, result);
        fileSystem.write(result.log, new File(path, "log.txt"), Charsets.UTF_8);
    }

    private void createLayoutFiles(File path, File templateDir, CalculationResult result)
    {
        Templater templater = templateManager.loadTemplate(templateDir, path);
        result.data.accept(new TemplateWritingVisitor(templater));
    }

    private void createResultDir(final File path, CalculationResult result)
    {
        if (!fileSystem.exists(path))
        {
            fileSystem.mkdir(path);
        }
        PlotDataVisitor visitor = new WriterVisitor(path);
        result.data.accept(visitor);
    }

    private final class WriterVisitor implements PlotDataVisitor
    {
        private final File path;

        private WriterVisitor(File path)
        {
            this.path = path;
        }

        @Override
        public void process1D(String name, double[] x, double[] y)
        {
            Map<String, double[]> dataMap = ImmutableMap.of(name, y);
            File outPath = new File(path, name + ".dat");
            fileSystem.writeQuietly(DataFile.createFile(name, x, dataMap), outPath);
        }

        @Override
        public void process2D(String name, double[][] x, double[][] y, double[][] val)
        {
            File outPath = new File(path, name + ".dat");
            fileSystem.writeQuietly(new DatFile2d(name, name, x, y, val), outPath);
        }
    }

}
