package ru.vasily.core.dataobjs;

import java.util.List;
import java.util.Map;

public class MapDataObject implements DataObject
{
    private final Map<String, ?> data;

    public MapDataObject(Map<String, ?> data)
    {
        this.data = data;
    }

    @Override
    public boolean has(String valueName)
    {
        return data.get(valueName) != null;
    }

    @Override
    public double getDouble(String valueName)
    {
        return (Double) data.get(valueName);
    }

    @Override
    public int getInt(String valueName)
    {
        return (Integer) data.get(valueName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataObject getObj(String valueName)
    {
        return (DataObject) data.get(valueName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataObject> getObjects(String listName)
    {
        return (List<DataObject>) data.get(listName);
    }

    @Override
    public String getString(String valueName)
    {
        return (String) data.get(valueName);
    }

}
