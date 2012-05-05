package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class StringServiceWithTopComponentDependency implements StringService
{
    private final TopComponent topComponent;

    public StringServiceWithTopComponentDependency(@DIKey("topComponent")TopComponent topComponent)
    {
        this.topComponent = topComponent;
    }

    @Override
    public String produceString()
    {
        return topComponent.getString1();
    }
}
