package ru.vasily.core.di.scopedriven;

public class ClassAndSDModule
{
    public final Class<?> clazz;
    public final SDModule module;

    public ClassAndSDModule(Class<?> clazz, SDModule module)
    {
        this.clazz = clazz;
        this.module = module;
    }
}
