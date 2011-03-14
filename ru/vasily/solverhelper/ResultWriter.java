package ru.vasily.solverhelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataFile;
import ru.vasily.dataobjs.DataObj;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.misc.FileTypeFilter;
import ru.vasily.solverhelper.misc.ILogger;
import ru.vasily.solverhelper.tecplot.ITecplotManager;

public class ResultWriter implements IResultWriter {

	private final ILogger logger;
	private final ITemplateManager templateManager;

	public ResultWriter(ILogger logger, ITemplateManager templateManager) {
		this.logger = logger;
		this.templateManager = templateManager;
	}

	@Override
	public void createResultDir(File path, CalculationResult result, File templateDir)
			throws IOException {
		createResultDir(path, result);
		templateManager.createLayoutFiles(templateDir, result, path);
		Iterable<File> macroses = Lists.newArrayList(path
				.listFiles((FilenameFilter)new FileTypeFilter("mcr")));
		Files.write(result.getLog(), new File(path, "log.txt"), Charsets.UTF_8);
//		tecplotManager.runMacro(macroses);
	}

	@Override
	public void createResultDir(File path, CalculationResult result)
			throws IOException {
		if (!path.exists()) {
			path.mkdirs();
		}
		for (DataObj data : result.getData()) {
			Map<String, double[]> dataMap = ImmutableMap.of(data.getKey(),
					data.getArray());
			File outPath = new File(path, data.getParams().get(
					DataObj.VALUE_NAME)
					+ ".dat");
			logger.log("creating file " + outPath.getAbsolutePath());
			DataFile.createFile(data.getParams().get(DataObj.VALUE_NAME),
					data.getxArray(), dataMap, outPath);
		}
	}

	private void copyFilesFromDir(File from, File to, boolean overwrite)
			throws IOException {
		for (File f : from.listFiles()) {
			File newFile = new File(to, f.getName());
			if (!newFile.exists() || overwrite) {
				Files.copy(f, newFile);
			}
		}
	}

}
