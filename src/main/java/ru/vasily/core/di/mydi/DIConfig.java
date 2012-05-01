package ru.vasily.core.di.mydi;

public interface DIConfig
{
    Object getImplByClass(Class<?> clazz);

    Object getImplByKey(Object key);
}
