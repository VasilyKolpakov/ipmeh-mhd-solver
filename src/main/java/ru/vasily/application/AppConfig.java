package ru.vasily.application;

import ru.vasily.core.io.RealFileSystem;
import ru.vasily.core.templates.SimpleFileTemplater;
import ru.vasily.core.templates.VelocityTemplateEngine;
import ru.vasily.core.dataobjs.JacksonDataObjService;
import ru.vasily.mydi.AbstractDIConfig;

import ru.vasily.solver.factory.DispatcherMHDSolverFactory;
import ru.vasily.solver.factory.MHDSolver1DFactory;
import ru.vasily.solver.factory.MHDSolver2DFactory;
import ru.vasily.solver.factory.RestoratorFactory;
import ru.vasily.application.misc.Logger;
import ru.vasily.application.misc.Serializer;
import ru.vasily.application.tecplot.MacroRunner;
import ru.vasily.application.tecplot.TecplotManager;

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

        addImpl(ApplicationMain.class);
        addImpl(MHDSolverFacade.class);
        addImpl(TemplatingResultWriter.class);
        addImpl(SimpleFileTemplater.class);
        addImpl(VelocityTemplateEngine.class);

        addImpl(DispatcherMHDSolverFactory.class);
        addImplIgnoringInterface(MHDSolver1DFactory.class);
        addImplIgnoringInterface(MHDSolver2DFactory.class);
        addImpl(RestoratorFactory.class);

    }

}