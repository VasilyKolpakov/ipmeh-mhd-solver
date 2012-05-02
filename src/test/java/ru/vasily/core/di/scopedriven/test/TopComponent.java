package ru.vasily.core.di.scopedriven.test;

import ru.vasily.core.di.DIKey;

import java.util.List;

import static java.util.Arrays.asList;

public class TopComponent
{
    private final StringService service1;
    private final StringService service2;

    public TopComponent(@DIKey("service1") StringService service1,
                        @DIKey("service2") StringService service2)
    {
        this.service1 = service1;
        this.service2 = service2;
    }

    public String getString1()
    {
        return service1.produceString();
    }

    public String getString2()
    {
        return service2.produceString();
    }
}
