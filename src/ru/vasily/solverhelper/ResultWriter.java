package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.*;
import com.google.common.io.Files;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataFile;
import ru.vasily.dataobjs.DataObj;
import ru.vasily.solverhelper.misc.ILogger;

public class ResultWriter implements IResultWriter {
	private static final Function<DataObj, Map<String, String>> DATA_OBJ_TO_PARAMS_MAP = new Function<DataObj, Map<String, String>>() {

		@Override
		public Map<String, String> apply(DataObj input) {
			return input.getParams();
		}

	};
	private final ILogger logger;
	private final ITemplateManager templateManager;

	public ResultWriter(ILogger logger, ITemplateManager templateManager) {
		this.logger = logger;
		this.templateManager = templateManager;
	}

	@Override
	public void createResultDir(File path, CalculationResult result,
			File templateDir) throws IOException {
		createResultDir(path, result);
		Iterable<Map<String, String>> data = transform(result.getData(),
				DATA_OBJ_TO_PARAMS_MAP);
		templateManager.createLayoutFiles(templateDir, data, path);
		Files.write(result.getLog(), new File(path, "log.txt"), Charsets.UTF_8);
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
//			logger.log("creating file " + outPath.getAbsolutePath());
			DataFile.createFile(data.getParams().get(DataObj.VALUE_NAME),
					data.getxArray(), dataMap, outPath);
		}
	}
}
