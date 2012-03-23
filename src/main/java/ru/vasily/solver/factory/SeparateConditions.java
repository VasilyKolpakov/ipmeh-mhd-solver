package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.BorderConditionsFactory;
import ru.vasily.solver.border.ContinuationBCF;
import ru.vasily.solver.border.PeriodicBCF;
import ru.vasily.solver.initialcond.*;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vasily.solver.initialcond.Array2dFiller.parseInitialConditions;

class SeparateConditions implements ConditionsFactory
{
    private final Map<String, BorderConditionsFactory> borderConditions = ImmutableMap
            .<String, BorderConditionsFactory>builder().
                    put("continuation", new ContinuationBCF()).
                    put("periodic", new PeriodicBCF()).
                    build();

    private double[][][] initialValues2d(DataObject params)
    {
        DataObject calculationConstants = params.getObj("calculationConstants");
        DataObject physicalConstants = params.getObj("physicalConstants");
        List<DataObject> initial_conditions_2d = params.getObjects("initial_conditions_2d");
        return parseInitialConditions(calculationConstants, physicalConstants, initial_conditions_2d);
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
