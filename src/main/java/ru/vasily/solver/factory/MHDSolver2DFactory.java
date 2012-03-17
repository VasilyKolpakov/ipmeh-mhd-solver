package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.parallel.ParallelEngine;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solver.MHDSolver2D;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.ContinuationCondition;
import ru.vasily.solver.border.PeriondicConditions;
import ru.vasily.solver.initialcond.Array2dFiller;
import ru.vasily.solver.initialcond.Function2DFactory;
import ru.vasily.solver.initialcond.Init2dFunction;
import ru.vasily.solver.initialcond.InitialConditionsFactories.*;
import ru.vasily.solver.initialcond.InitialValues2dBuilder;
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
        String type = restoratorData.getString("type");
        return restoratorFactory.createRestorator(type);
    }

    private interface BorderConditionsFactory
    {
        Array2dBorderConditions createConditions(DataObject params);
    }

    private static class ContinuationBCF implements BorderConditionsFactory
    {
        @Override
        public Array2dBorderConditions createConditions(DataObject allParams)
        {
            DataObject calculationConstants = allParams.getObj("calculationConstants");
            int xRes = calculationConstants.getInt("xRes");
            int yRes = calculationConstants.getInt("yRes");
            return new ContinuationCondition(xRes, yRes);
        }
    }

    private static class PeriodicBCF implements BorderConditionsFactory
    {
        @Override
        public Array2dBorderConditions createConditions(DataObject allParams)
        {
            DataObject calculationConstants = allParams.getObj("calculationConstants");
            int xRes = calculationConstants.getInt("xRes");
            int yRes = calculationConstants.getInt("yRes");
            return new PeriondicConditions(xRes, yRes);
        }
    }

    private static class SeparateConditions implements ConditionsFactory
    {
        private final Map<String, BorderConditionsFactory> borderConditions = ImmutableMap
                .<String, BorderConditionsFactory>builder().
                        put("continuation", new ContinuationBCF()).
                        put("periodic", new PeriodicBCF()).
                        build();
        private final Map<String, Function2DFactory> functionFactories = ImmutableMap
                .<String, Function2DFactory>builder().
                        put("fill_rect", new FillRect()).
                        put("fill_circle", new FillCircle()).
                        put("magnetic_charge_spot", new MagneticChargeSpot()).
                        put("rotor_problem", new RotorProblem()).
                        put("orsag_tang_vortex", new OrsagTangVortex()).
                        put("kelvin_helmholtz", new KelvinHelmholtz()).
                        build();

        private double getDouble(DataObject data, String valueName, double default_)
        {
            if (data.has(valueName))
            {
                return data.getDouble(valueName);
            }
            else
            {
                return default_;
            }
        }

        private double[][][] initialValues2d(DataObject params)
        {
            DataObject calculationConstants = params.getObj("calculationConstants");
            DataObject physicalConstants = params.getObj("physicalConstants");
            int xRes = calculationConstants.getInt("xRes");
            int yRes = calculationConstants.getInt("yRes");
            double xLength = physicalConstants.getDouble("xLength");
            double yLength = physicalConstants.getDouble("yLength");
            double x_0 = getDouble(physicalConstants, "x_0", 0.0);
            double y_0 = getDouble(physicalConstants, "y_0", 0.0);

            InitialValues2dBuilder<double[][][]> builder = new Array2dFiller(xRes, yRes, x_0, y_0, xLength,
                    yLength);
            for (DataObject initData : params.getObjects("initial_conditions_2d"))
            {
                Init2dFunction function2D = functionFactories.get(initData.getString("type"))
                        .createFunction(initData, physicalConstants);
                builder.apply(function2D);
            }
            return builder.build();
        }

        private Array2dBorderConditions borderConditions(DataObject params)
        {
            String type = params.getObj("border_conditions").getString("type");
            BorderConditionsFactory factory = borderConditions.get(type);
            checkNotNull(factory, "not supported border conditions type '%s', supported are %s", type,
                    borderConditions.keySet());
            return factory.createConditions(params);
        }

        @Override
        public Conditions createConditions(DataObject data)
        {
            return new Conditions(borderConditions(data), initialValues2d(data));
        }
    }

    private interface ConditionsFactory
    {
        Conditions createConditions(DataObject data);
    }

    private static class Conditions
    {
        public final Array2dBorderConditions borderConditions;
        public final double[][][] initialConditions;

        private Conditions(Array2dBorderConditions borderConditions, double[][][] initialConditions)
        {
            this.borderConditions = borderConditions;
            this.initialConditions = initialConditions;
        }
    }
}
