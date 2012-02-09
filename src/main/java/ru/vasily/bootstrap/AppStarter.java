package ru.vasily.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ru.vasily.core.FileSystem;
import ru.vasily.core.parallel.FutureBasedParallelEngine;
import ru.vasily.core.parallel.NoOpParallelEngine;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.AppConfig;
import ru.vasily.solverhelper.ApplicationMain;
import ru.vasily.solverhelper.appstrategy.AppStrategy;
import ru.vasily.solverhelper.appstrategy.IterativeAppStrategy;
import ru.vasily.solverhelper.appstrategy.LongTaskAppStrategy;
import ru.vasily.solverhelper.appstrategy.SimpleAppStrategy;
import ru.vasily.solverhelper.misc.DataObjectParser;

public class AppStarter
{
    private final DataObjectService objectService;
    private final FileSystem fileSystem;
    private final Map<String, Class<? extends AppStrategy>> strategies = ImmutableMap
            .<String, Class<? extends AppStrategy>>builder().
                    put("simple", SimpleAppStrategy.class).
                    put("iterative", IterativeAppStrategy.class).
                    put("long_task", LongTaskAppStrategy.class).
                    build();

    public AppStarter(DataObjectService objectService, FileSystem fileSystem)
    {
        this.objectService = objectService;
        this.fileSystem = fileSystem;
    }

    private void startApp(String paramsFileName) throws IOException
    {
        DataObject params = fileSystem.parse(new DataObjectParser(objectService),
                                             new File(paramsFileName));
        AppConfig config = new AppConfig();
        addParallelEngine(params, config);
        addAppSrategy(params, config);
        startMainApp(params.getObj("directories"), config);
    }

    private void startMainApp(DataObject params, AppConfig config)
    {
        new MyDI(config).getInstanceViaDI(ApplicationMain.class).
                execute(params.getString("input"), params.getString("output"),
                        params.getString("template"));
    }

    private void addAppSrategy(DataObject params, AppConfig config)
    {
        if (!params.has("app_strategy"))
        {
            config.registerComponent(AppStrategy.class, SimpleAppStrategy.class);
        }
        else
        {
            String strategyName = params.getString("app_strategy");
            Class<? extends AppStrategy> strategy = strategies.get(strategyName);
            Preconditions.checkNotNull(strategy, "there is no strategy named %s, only {%s}",
                                       strategyName, strategies.keySet());
            config.registerComponent(AppStrategy.class, strategy);
        }
    }

    private void addParallelEngine(DataObject params, AppConfig config)
    {
        if (!params.has("number_of_threads"))
        {
            config.addObject(NoOpParallelEngine.INSTANCE);
        }
        else
        {
            int numberOfThreads = params.getInt("number_of_threads");
            Preconditions.checkArgument(numberOfThreads > 0,
                                        "number  of threads must be > 0, not %s", numberOfThreads);
            config.addObject(new FutureBasedParallelEngine(numberOfThreads));
        }
    }

    public static void main(String[] args) throws IOException
    {
        MyDI myDI = new MyDI(new BootstrapConfig());
        myDI.getInstanceViaDI(AppStarter.class).startApp(args[0]);
    }
}
