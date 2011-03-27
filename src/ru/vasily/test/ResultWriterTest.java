package ru.vasily.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObj;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.AppConfig;
import ru.vasily.solverhelper.ITemplateManager;
import ru.vasily.solverhelper.ResultWriter;
import ru.vasily.solverhelper.misc.ILogger;
import ru.vasily.solverhelper.misc.Logger;
import ru.vasily.solverhelper.tecplot.ITecplotManager;

public class ResultWriterTest {
	private static final class ITecplotManagerImplementation implements
			ITecplotManager {
		@Override
		public void runMacro(Iterable<File> macro) {
			// TODO Auto-generated method stub

		}

		@Override
		public void runMacro(File macro) {
			// TODO Auto-generated method stub

		}
	}

	private static final class ITemplateManagerImplementation implements
			ITemplateManager {

		@Override
		public void createLayoutFiles(File templateDir, Iterable<Map<String, String>> data, File outputDir)
				throws IOException {
			// TODO Auto-generated method stub
			
		}
	}

	private static final class ILoggerImplementation implements ILogger {
		@Override
		public void log(String log) {
			// TODO Auto-generated method stub

		}
	}

	static void testCreateDir() throws IOException {
		ResultWriter writer = new ResultWriter(new ILoggerImplementation(),
				new ITemplateManagerImplementation());
		File test = new File("test");
		DataObj dataobj = new DataObj("key", new double[] { 2, 3 },
				new double[] { 1, 3 }, ImmutableMap.of("prop", "val"));
		writer.createResultDir(test,
				new CalculationResult(Collections.singletonList(dataobj),""));
	}

	static void integrationTest() throws IOException {
		ResultWriter test = new MyDI(new AppConfig())
				.getInstanceViaDI(ResultWriter.class);
		String MAX_X = "max_x";
		String MIN_X = "min_x";
		String MAX_Y = "max_y";
		String MIN_Y = "min_y";
		String VALUE_NAME = "value_name";

		Map<String, String> params = ImmutableMap.of(MAX_X, "2", MAX_Y, "4",
				MIN_X, "0", MIN_Y, "0", VALUE_NAME, "name");
		DataObj element = new DataObj("name", new double[] { 0, 4 },
				new double[] { 0, 2 }, params);
		List<DataObj> data = ImmutableList.of(element);
		CalculationResult result = new CalculationResult(data,"");
		test.createResultDir(new File("output\\"), result, new File(
				"template\\"));

	}

	public static void main(String[] args) throws Exception {
		testCreateDir();
	}
}
