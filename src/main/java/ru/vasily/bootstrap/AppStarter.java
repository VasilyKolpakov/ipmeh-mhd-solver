package ru.vasily.bootstrap;

import java.io.IOException;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ru.vasily.core.io.FileSystem;
import ru.vasily.core.parallel.FutureBasedParallelEngine;
import ru.vasily.core.parallel.NoOpParallelEngine;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjectService;
import ru.vasily.core.di.mydi.MyDI;
import ru.vasily.application.AppConfig;
import ru.vasily.application.ApplicationMain;
import ru.vasily.application.appstrategy.AppStrategy;
import ru.vasily.application.appstrategy.IterativeAppStrategy;
import ru.vasily.application.appstrategy.LongTaskAppStrategy;
import ru.vasily.application.appstrategy.SimpleAppStrategy;

import static ru.vasily.application.ApplicationParamsConstants.*;
import static ru.vasily.application.misc.DataObjectParser.asDataObject;

public class AppStarter
{
    public static final String CONFIG_PATH = "config.js";
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

    private void startApp(String appStrategyName) throws IOException
    {
        DataObject params = fileSystem.parse(CONFIG_PATH, asDataObject(objectService));
        AppConfig config = new AppConfig();
        addParallelEngine(params, config);
        addAppSrategy(appStrategyName, config);
        config.registerComponentWithKey(DIRECTORIES_DI_KEY, params.getObj("directories"));
        startMainApp(params.getObj("directories"), config);
    }

    private void startMainApp(DataObject params, AppConfig config)
    {
        new MyDI(config).getInstanceViaDI(ApplicationMain.class).
                execute(params.getString("input"));
    }

    private void addAppSrategy(String strategyName, AppConfig config)
    {
            Class<? extends AppStrategy> strategy = strategies.get(strategyName);
            Preconditions.checkNotNull(strategy, "there is no strategy named %s, only {%s}",
                                       strategyName, strategies.keySet());
            config.registerComponent(AppStrategy.class, strategy);
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
