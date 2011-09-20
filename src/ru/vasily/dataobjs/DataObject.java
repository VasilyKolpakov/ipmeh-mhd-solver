package ru.vasily.dataobjs;

import java.util.List;

public interface DataObject
{
	double getDouble(String valueName);

	int getInt(String valueName);

	String getString(String valueName);

	DataObject getObj(String valueName);

	List<DataObject> getObjects(String listName);
}
