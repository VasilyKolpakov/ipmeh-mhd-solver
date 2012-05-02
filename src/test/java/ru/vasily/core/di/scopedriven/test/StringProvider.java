package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class StringProvider
{
    private final String string;

    public StringProvider(@DIKey("string") String string)
    {
        this.string = string;
    }

    public String getString()
    {
        return string;
    }
}
