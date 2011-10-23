package ru.vasily.solverhelper;

import ru.vasily.core.RealFileSystem;
import ru.vasily.dataobjs.JacksonDataObjService;
import ru.vasily.mydi.AbstractDIConfig;

import ru.vasily.solver.factory.DispatcherMHDSolverFactory;
import ru.vasily.solver.factory.MHDSolver1DFactory;
import ru.vasily.solver.factory.MHDSolver2DFactory;
import ru.vasily.solver.factory.RestoratorFactory;
import ru.vasily.solverhelper.misc.Logger;
import ru.vasily.solverhelper.misc.Serializer;
import ru.vasily.solverhelper.misc.StringParameterizerFactory;
import ru.vasily.solverhelper.tecplot.TecplotManager;

public class AppConfig extends AbstractDIConfig
{

	@Override
	public void initConfig()
	{
		addImpl(RealFileSystem.class);
		addImpl(Logger.class);
		addImpl(JacksonDataObjService.class);

		addImpl(Serializer.class);

		addImpl(TecplotManager.class);
		addImpl(MacroRunner.class);

		addImpl(StringParameterizerFactory.class);
		addImpl(TemplateManager.class);

		addImpl(ApplicationMain.class);
		addImpl(Solver.class);
		addImpl(ResultWriter.class);

		addImpl(DispatcherMHDSolverFactory.class);
		addImplIgnoringInterface(MHDSolver1DFactory.class);
		addImplIgnoringInterface(MHDSolver2DFactory.class);
		addImpl(RestoratorFactory.class);
	}

}