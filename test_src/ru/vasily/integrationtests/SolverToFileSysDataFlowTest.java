package ru.vasily.integrationtests;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.mockito.Mockito.*;

import ru.vasily.core.FileSystem;
import ru.vasily.core.FileSystem.Parser;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.dataobjs.MapDataObject;
import ru.vasily.mydi.AbstractDIConfig;
import ru.vasily.mydi.DIConfig;
import ru.vasily.mydi.MyDI;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solverhelper.ApplicationMain;
import ru.vasily.solverhelper.ISolverFactory;
import ru.vasily.solverhelper.ITemplateManager;
import ru.vasily.solverhelper.ResultWriter;
import ru.vasily.solverhelper.Solver;
import ru.vasily.solverhelper.misc.ISerializer;

public class SolverToFileSysDataFlowTest {
	private static final String TEMPLATE_DIR = "templateDir";
	private static final String OUTPUT_PATH = "outputPath";
	private static final String PARAMS_PATH = "paramsPath";
	private ApplicationMain application;
	private ITemplateManager templateManager;
	private MHDSolver mhdSolver;
	private FileSystem fileSystem;

	@Before
	public void setup() {
		final DataObjectService paramsLoader = mock(DataObjectService.class);
		final ISerializer serializer = mock(ISerializer.class);
		final ISolverFactory solverFactory = mock(ISolverFactory.class);
		fileSystem = mock(FileSystem.class);
		mhdSolver = mock(MHDSolver.class);
		templateManager = mock(ITemplateManager.class);
		DIConfig diConfig = new AbstractDIConfig() {

			@Override
			public void initConfig() {
				addImpl(ApplicationMain.class);
				addImpl(Solver.class);
				addImpl(ResultWriter.class);
				registerComponent(FileSystem.class, fileSystem);
				registerComponent(ISolverFactory.class, solverFactory);
				registerComponent(ISerializer.class, serializer);
				registerComponent(DataObjectService.class, paramsLoader);
				registerComponent(ITemplateManager.class, templateManager);
			}
		};
		when(solverFactory.createSolver(any(DataObject.class))).thenReturn(mhdSolver);
		application = new MyDI(diConfig).getInstanceViaDI(ApplicationMain.class);
	}

	@Test
	public void test() throws IOException {
		double min_x = 0;
		double max_x = 1;
		String name = "test_data";
		double min_y = 1;
		double max_y = 2;
		File paramsFile = new File("param.js");
		prepareDataStub(paramsFile);
		prepateOutputData(min_x, max_x, name, min_y, max_y);
		application.execute(PARAMS_PATH, OUTPUT_PATH, TEMPLATE_DIR, false);
		Map<String, String> expected_template_params = outputParams(min_x, max_x, name, min_y,
				max_y);
		Iterable<Map<String, String>> paramsIterable = ImmutableList
				.<Map<String, String>> builder().add(expected_template_params).build();
		verify(templateManager).createLayoutFiles(eq(new File(TEMPLATE_DIR)),
				argThat(equal(paramsIterable)), eq(new File(OUTPUT_PATH, "param")));
	}

	private Matcher<Iterable<Map<String, String>>> equal(final Iterable<Map<String, String>> paramsIterable) {
		return new BaseMatcher<Iterable<Map<String, String>>>() {

			@Override
			public boolean matches(Object item) {
				if (item instanceof Iterable)
				{
					Iterator<Map<String, String>> iteratorItem = ((Iterable<Map<String, String>>) item)
							.iterator();
					Iterator<Map<String, String>> paramsIterator = paramsIterable.iterator();
					while (iteratorItem.hasNext() && paramsIterator.hasNext())
					{
						Map<String, String> params = paramsIterator.next();
						Map<String, String> actualParams = iteratorItem.next();

						for (String key : params.keySet())
						{
							if (!params.get(key).equals(actualParams.get(key)))
							{
								return false;
							}
						}
					}
					if (iteratorItem.hasNext() != paramsIterator.hasNext())
					{
						return false;
					}
					return true;
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an Iterable that is equal to ").appendValue(paramsIterable);
			}
		};
	}

	private void prepateOutputData(double min_x, double max_x, String name, double min_y, double max_y) {
		when(mhdSolver.getTotalTime()).thenReturn(Double.POSITIVE_INFINITY);
		when(mhdSolver.getTotalTime()).thenReturn(Double.POSITIVE_INFINITY);
		when(mhdSolver.getXCoord()).thenReturn(new double[] { min_x, max_x });
		when(mhdSolver.getData()).thenReturn(
				ImmutableMap.<String, double[]> of(name, new double[] { min_y, max_y }));
	}

	private void prepareDataStub(File paramsFile) throws IOException {
		when(fileSystem.listFiles(
				eq(new File(PARAMS_PATH)), any(FilenameFilter.class))).thenReturn(
				new File[] { paramsFile });
		when(fileSystem.parse(any(Parser.class), eq(paramsFile))).thenReturn(dataStub());
	}

	private ImmutableMap<String, String> outputParams(double min_x, double max_x, String name, double min_y, double max_y) {
		return ImmutableMap.<String, String> builder()
				.put("min_x", String.valueOf(min_x))
				.put("max_x", String.valueOf(max_x))
				.put("value_name", String.valueOf(name))
				.put("min_y", String.valueOf(min_y))
				.put("max_y", String.valueOf(max_y))
				.build();
	}

	private static DataObject dataStub() {
		Map<String, Object> physicalConstants = ImmutableMap.<String, Object> builder()
				.put("totalTime", 0.02)
				.build();
		Map<String, Object> data = ImmutableMap.<String, Object> builder()
				.put("physicalConstants", physicalConstants)
				.build();
		return new MapDataObject(data);
	}

}
