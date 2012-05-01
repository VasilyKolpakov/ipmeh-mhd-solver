package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class HelloComp implements Comp
{
    private final GreetingProvider greetingProvider;

    public HelloComp(@DIKey("greetingProvider") GreetingProvider greetingProvider)
    {
        this.greetingProvider = greetingProvider;
    }

    @Override
    public void go()
    {
        System.out.println(greetingProvider.getGreeting() + " world");
    }
}
