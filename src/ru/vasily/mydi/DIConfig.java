package ru.vasily.mydi;

public interface DIConfig
{
	Object getImplByClass(Class<?> clazz);

	Object getImplByKey(Object key);
}
