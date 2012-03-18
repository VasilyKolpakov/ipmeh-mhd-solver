package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;

interface ConditionsFactory
{
    Conditions createConditions(DataObject data);
}
