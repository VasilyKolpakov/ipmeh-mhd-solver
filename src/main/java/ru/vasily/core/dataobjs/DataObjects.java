package ru.vasily.core.dataobjs;

import java.util.Map;

public class DataObjects
{
    public static DataObject mapAsDataObj(Map<String, ?> data)
    {
        return new MapDataObject(data);
    }

    public static double getDouble(DataObject data, String valueName, double default_)
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
}
