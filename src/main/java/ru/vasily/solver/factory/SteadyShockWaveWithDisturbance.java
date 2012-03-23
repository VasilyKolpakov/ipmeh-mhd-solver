package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.math.Complex;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.ContinuationBCF;

import java.util.List;

import static com.google.common.primitives.Doubles.asList;

public class SteadyShockWaveWithDisturbance implements ConditionsFactory
{
    @Override
    public Conditions createConditions(DataObject data)
    {
        Array2dBorderConditions conditions = new ContinuationBCF().createConditions(data);
        return new Conditions(conditions, null);
    }

}
