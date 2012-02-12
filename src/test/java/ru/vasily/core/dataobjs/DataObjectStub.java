package ru.vasily.core.dataobjs;

import java.util.Collections;
import java.util.List;

public class DataObjectStub implements DataObject
{

    @Override
    public double getDouble(String valueName)
    {
        return 0;
    }

    @Override
    public int getInt(String valueName)
    {
        return 0;
    }

    @Override
    public String getString(String valueName)
    {
        return "";
    }

    @Override
    public DataObject getObj(String valueName)
    {
        return new DataObjectStub();
    }

    @Override
    public List<DataObject> getObjects(String listName)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean has(String valueName)
    {
        return false;
    }

}
