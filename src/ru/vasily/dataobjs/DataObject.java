package ru.vasily.dataobjs;

public interface DataObject {
	double getDouble(String valueName);

	int getInt(String valueName);

	String getString(String valueName);

	DataObject getObj(String valueName);
}
