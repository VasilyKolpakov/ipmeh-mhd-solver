package ru.vasily.solverhelper;

import ru.vasily.core.RealFileSystem;
import ru.vasily.dataobjs.JacksonDataObjService;
import ru.vasily.mydi.AbstractDIConfig;

import ru.vasily.solverhelper.misc.Logger;
import ru.vasily.solverhelper.misc.Serializer;
import ru.vasily.solverhelper.misc.StringParameterizerFactory;
import ru.vasily.solverhelper.tecplot.TecplotManager;

public class AppConfig extends AbstractDIConfig {

	@Override
	public void initConfig() {
		addImpl(ApplicationMain.class);
		addImpl(Solver.class);
		addImpl(ResultWriter.class);
		addImpl(Logger.class);
		addImpl(TecplotManager.class);
		addImpl(StringParameterizerFactory.class);
		addImpl(TemplateManager.class);
		addImpl(Serializer.class);
		addImpl(MacroRunner.class);
		addImpl(JacksonDataObjService.class);
		addImpl(SolverFactory.class);
		addImpl(RealFileSystem.class);
	}
}