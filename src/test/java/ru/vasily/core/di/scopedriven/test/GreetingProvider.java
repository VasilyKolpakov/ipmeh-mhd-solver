package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class GreetingProvider
{
    private final String greeting;

    public GreetingProvider(@DIKey("greeting") String greeting)
    {
        this.greeting = greeting;
    }

    public String getGreeting()
    {
        return greeting;
    }
}
