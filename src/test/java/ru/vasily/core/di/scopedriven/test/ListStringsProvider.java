package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

import java.util.List;

public class ListStringsProvider
{
    private final List<String> strings;

    public ListStringsProvider(@DIKey("strings") List<String> strings)
    {
        this.strings = strings;
    }

    public List<String> getStrings()
    {
        return strings;
    }
}
