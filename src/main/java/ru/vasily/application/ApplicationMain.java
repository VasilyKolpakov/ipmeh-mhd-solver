package ru.vasily.application;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


import com.google.common.base.Predicate;
import ru.vasily.core.io.FileSystem;
import ru.vasily.application.appstrategy.AppStrategy;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class ApplicationMain
{
    private static final Predicate<String> JS_FILES_FILTER = new Predicate<String>()
    {
        Pattern namePattern = Pattern.compile(".js$",
                                              Pattern.CASE_INSENSITIVE);

        @Override
        public boolean apply(@Nullable String path)
        {
            return namePattern.matcher(path).find();
        }
    };

    private static final String PARAMS_FILE_EXTENSION = "js";
    private final FileSystem fileSystem;
    private final AppStrategy appStrategy;

    public ApplicationMain(FileSystem fileSystem, AppStrategy appStrategy)
    {
        this.appStrategy = appStrategy;
        this.fileSystem = fileSystem;
    }

    public void execute(String inputDir)
    {
        List<String> inputPaths = getInputPaths(inputDir);
        for (String inputFile : inputPaths)
        {
            try
            {
                appStrategy.processInputFile(inputFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private List<String> getInputPaths(String inputPath)
    {
        List<String> allPaths = fileSystem.listDirContents(inputPath);
        Iterable<String> inputPaths = filter(allPaths, JS_FILES_FILTER);
        List<String> sortedInputPaths = newArrayList(inputPaths);
        Collections.sort(sortedInputPaths);
        return sortedInputPaths;
    }

}
