package ru.vasily.core.di.mydi.test;

import ru.vasily.core.di.mydi.AbstractDIConfig;
import ru.vasily.core.di.mydi.MyDI;

public class TestAbstractConfig extends AbstractDIConfig
{

    @Override
    public void initConfig()
    {
        addImpl(A.class);
        addImpl(B.class);
        addImpl(C.class);
    }

    public static void main(String[] args)
    {
        MyDI di = new MyDI(new TestConfig());
        IC c = di.getInstanceViaDI(IC.class);
        System.out.println(c.getA());
    }
}
