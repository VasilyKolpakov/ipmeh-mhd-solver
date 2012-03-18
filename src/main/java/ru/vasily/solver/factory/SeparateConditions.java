package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.BorderConditionsFactory;
import ru.vasily.solver.border.ContinuationBCF;
import ru.vasily.solver.border.PeriodicBCF;
import ru.vasily.solver.initialcond.*;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class SeparateConditions implements ConditionsFactory
{
    private final Map<String, BorderConditionsFactory> borderConditions = ImmutableMap
            .<String, BorderConditionsFactory>builder().
                    put("continuation", new ContinuationBCF()).
                    put("periodic", new PeriodicBCF()).
                    build();
    private final Map<String, Function2DFactory> functionFactories = ImmutableMap
            .<String, Function2DFactory>builder().
                    put("fill_rect", new InitialConditionsFactories.FillRect()).
                    put("fill_circle", new InitialConditionsFactories.FillCircle()).
                    put("magnetic_charge_spot", new InitialConditionsFactories.MagneticChargeSpot()).
                    put("rotor_problem", new InitialConditionsFactories.RotorProblem()).
                    put("orsag_tang_vortex", new InitialConditionsFactories.OrsagTangVortex()).
                    put("kelvin_helmholtz", new InitialConditionsFactories.KelvinHelmholtz()).
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
