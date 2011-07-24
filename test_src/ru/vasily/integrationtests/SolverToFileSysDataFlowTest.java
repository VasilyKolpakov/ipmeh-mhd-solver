package ru.vasily.integrationtests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import ru.vasily.core.FileSystem;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
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
	public void test() {
		when(fileSystem.listFiles(
				eq(new File(PARAMS_PATH)), any(FilenameFilter.class))).thenReturn(
				new File[] { new File("param") });
		application.execute(PARAMS_PATH, "outputPath", "templateDir", false);
	}
}
