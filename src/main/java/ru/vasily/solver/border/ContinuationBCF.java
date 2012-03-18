package ru.vasily.solver.border;

import ru.vasily.core.dataobjs.DataObject;

public class ContinuationBCF implements BorderConditionsFactory
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
