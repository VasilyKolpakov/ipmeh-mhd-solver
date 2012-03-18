package ru.vasily.solver.border;

import ru.vasily.core.dataobjs.DataObject;

public interface BorderConditionsFactory
{
    Array2dBorderConditions createConditions(DataObject params);
}
