package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.parallel.ParallelEngine;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solver.MHDSolver2D;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RiemannSolver1Dto2DWrapper;
import ru.vasily.solver.riemann.RoeSolverByKryukov;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class MHDSolver2DFactory implements IMHDSolverFactory
{
    private final RestoratorFactory restoratorFactory;
    private final ParallelEngine parallelEngine;
    private final Map<String, ConditionsFactory> conditionFactories = ImmutableMap.<String, ConditionsFactory>builder()
            .put("separate", new SeparateConditions())
            .put("steady_shock_wave", new SteadyShockWaveWithDisturbance())
            .put("steady_shock_wave_alfven", new SteadyShockWaveWithAlfvenWave())
            .build();

    public MHDSolver2DFactory(RestoratorFactory restoratorFactory, ParallelEngine parallelEngine)
    {
        this.restoratorFactory = restoratorFactory;
        this.parallelEngine = parallelEngine;
    }

    @Override
    public MHDSolver createSolver(DataObject params)
    {
        Conditions conditions = createConditions(params);
        return new MHDSolver2D(params, restorator(params), new RiemannSolver1Dto2DWrapper(
                new RoeSolverByKryukov()),
                conditions.borderConditions,
                parallelEngine,
                conditions.initialConditions);

    }

    private Conditions createConditions(DataObject params)
    {
        String type = params.getString("conditions_input_type");
        ConditionsFactory conditionsFactory = conditionFactories.get(type);
        checkNotNull(conditionsFactory, "not supported conditions input type '%s', supported are %s", type,
                conditionFactories.keySet());
        return conditionsFactory.createConditions(params);
    }

    private ThreePointRestorator restorator(DataObject params)
    {
        DataObject restoratorData = params.getObj("restorator");
        return restoratorFactory.createRestorator(restoratorData);
    }

}
