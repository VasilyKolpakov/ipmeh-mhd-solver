package ru.vasily.mydi.test;

import ru.vasily.mydi.AbstractDIConfig;
import ru.vasily.mydi.MyDI;

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
