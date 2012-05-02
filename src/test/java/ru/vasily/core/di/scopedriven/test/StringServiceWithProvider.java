package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class StringServiceWithProvider implements StringService
{
    private final StringProvider stringProvider;

    public StringServiceWithProvider(@DIKey("stringProvider") StringProvider greetingProvider)
    {
        this.stringProvider = greetingProvider;
    }

    @Override
    public String produceString()
    {
        return stringProvider.getString();
    }
}
