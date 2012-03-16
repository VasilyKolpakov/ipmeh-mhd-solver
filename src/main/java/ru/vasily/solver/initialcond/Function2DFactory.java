package ru.vasily.solver.initialcond;

import ru.vasily.core.dataobjs.DataObject;

public interface Function2DFactory
{
    Init2dFunction createFunction(DataObject data, DataObject physicalConstants);
}
