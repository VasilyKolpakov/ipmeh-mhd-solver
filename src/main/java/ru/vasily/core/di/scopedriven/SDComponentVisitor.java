package ru.vasily.core.di.scopedriven;

import java.util.List;

public interface SDComponentVisitor<T>
{
    public T visitComplexComponent(ClassAndSDModule classAndSDModule);

    public T visitPrimitive(Object primitive);

    public T visitList(List<SDComponent> components);
}
