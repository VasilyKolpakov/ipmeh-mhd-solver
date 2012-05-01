package ru.vasily.core.di.mydi;

import org.junit.Test;

public class MyDITest
{
    @Test
    public void injection_by_key()
    {
        MyDI myDI = new MyDI(new AbstractDIConfig()
        {
            @Override
            public void initConfig()
            {
                registerComponentWithKey("key", "the object to inject");
            }
        });
        TestAnnotationKeyComponent test =
                myDI.getInstanceViaDI(TestAnnotationKeyComponent.class);
    }
}
