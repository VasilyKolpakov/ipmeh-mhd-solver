package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Iterables.*;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataFile;
import ru.vasily.dataobjs.ArrayDataObj;

public class ResultWriter implements IResultWriter {
	private static final Function<ArrayDataObj, Map<String, String>> DATA_OBJ_TO_PARAMS_MAP = new Function<ArrayDataObj, Map<String, String>>() {

		@Override
		public Map<String, String> apply(ArrayDataObj input) {
			return input.getParams();
		}

	};
	private final ITemplateManager templateManager;
	private final FileSystem fileSystem;

	public ResultWriter(ITemplateManager templateManager, FileSystem fileSystem) {
		this.templateManager = templateManager;
		this.fileSystem = fileSystem;
	}

	@Override
	public void createResultDir(File path, CalculationResult result,
			File templateDir) throws IOException {
		createResultDir(path, result);
		Iterable<Map<String, String>> data = transform(result.getData(),
				DATA_OBJ_TO_PARAMS_MAP);
		templateManager.createLayoutFiles(templateDir, data, path);
		fileSystem.write(result.getLog(), new File(path, "log.txt"), Charsets.UTF_8);
	}

	@Override
	public void createResultDir(File path, CalculationResult result)
			throws IOException {
		if (!fileSystem.exists(path))
		{
			fileSystem.mkdir(path);
		}
		for (ArrayDataObj data : result.getData())
		{
			Map<String, double[]> dataMap = ImmutableMap.of(data.getKey(),
					data.getArray());
			File outPath = new File(path, data.getParams().get(
					ArrayDataObj.VALUE_NAME)
					+ ".dat");

			fileSystem.write(
					DataFile.createFile(data.getParams().get(ArrayDataObj.VALUE_NAME),
							data.getxArray(), dataMap),
					outPath
					);
		}
	}

}
