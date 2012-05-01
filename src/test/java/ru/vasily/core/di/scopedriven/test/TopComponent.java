package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

public class TopComponent
{
    private final Comp comp1;
    private final Comp comp2;

    public TopComponent(@DIKey("comp1")Comp comp1,
                        @DIKey("comp2")Comp comp2)
    {
        this.comp1 = comp1;
        this.comp2 = comp2;
    }

    public void goAll()
    {
        comp1.go();
        comp2.go();
    }
}
