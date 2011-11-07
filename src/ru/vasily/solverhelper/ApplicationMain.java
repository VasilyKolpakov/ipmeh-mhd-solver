package ru.vasily.solverhelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import ru.vasily.core.FileSystem;
import ru.vasily.solverhelper.appstrategy.AppStrategy;
import ru.vasily.solverhelper.misc.FileTypeFilter;

public class ApplicationMain
{

	private static final String PARAMS_FILE_EXTENSION = "js";
	private final FileSystem fileSystem;
	private final AppStrategy appStrategy;

	public ApplicationMain(FileSystem fileSystem, AppStrategy appStrategy)
	{
		this.appStrategy = appStrategy;
		this.fileSystem = fileSystem;
	}

	public void execute(String inputDirString, String outputDirString,
			String templateDirString)
	{
		File inputPath = new File(inputDirString);
		File outputDir = new File(outputDirString);
		File templateDir = new File(templateDirString);
		List<File> inputPaths = getInputPaths(inputPath);
		for (File inputFile : inputPaths)
		{
			try
			{
				appStrategy.processInputFile(inputFile, templateDir, outputDir);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private List<File> getInputPaths(File inputPath)
	{
		List<File> inputPaths = fileSystem.listFiles(inputPath,
				(FilenameFilter) new FileTypeFilter(
						PARAMS_FILE_EXTENSION));
		Collections.sort(inputPaths, new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		return inputPaths;
	}

}
