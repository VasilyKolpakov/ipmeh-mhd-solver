package ru.vasily.dataobjs;

import java.util.Map;

public class MapDataObject extends DataObjectStub
{
	private final Map<String, Object> data;

	public MapDataObject(Map<String, Object> data)
	{
		this.data = data;
	}

	@Override
	public double getDouble(String valueName)
	{
		return ((Number) data.get(valueName)).doubleValue();
	}

	@Override
	public int getInt(String valueName)
	{
		return ((Number) data.get(valueName)).intValue();
	}

	@Override
	public DataObject getObj(String valueName)
	{
		return new MapDataObject((Map<String, Object>) data.get(valueName));
	}

	@Override
	public String getString(String valueName)
	{
		return (String) data.get(valueName);
	}

}
