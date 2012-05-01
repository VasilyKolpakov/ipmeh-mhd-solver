package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class HelloCompWithLogging implements Comp
{
    private final GreetingProvider greetingProvider;

    public HelloCompWithLogging(@DIKey("greetingProvider") GreetingProvider greetingProvider)
    {
        this.greetingProvider = greetingProvider;
    }

    @Override
    public void go()
    {
        System.out.println(greetingProvider.getGreeting() + " world");
        System.out.println(toString());
    }

    @Override
    public String toString()
    {
        return "HelloCompWithLogging{" +
                "greetingProvider=" + greetingProvider +
                '}';
    }
}
