package ru.vasily.core.di.mydi.test;

import java.util.HashMap;
import java.util.Map;

import ru.vasily.core.di.mydi.DIConfig;
import ru.vasily.core.di.mydi.MyDI;

public class TestConfig implements DIConfig
{
    private Map<Class<?>, Object> impls = new HashMap<Class<?>, Object>();

    public TestConfig()
    {
        impls.put(IA.class, new IA()
        {
            @Override
            public String toString()
            {
                return "aa";
            }
        });
        impls.put(IB.class, B.class);
        impls.put(IC.class, C.class);
    }

    @Override
    public Object getImplByClass(Class<?> clazz)
    {
        return impls.get(clazz);
    }

    public static void main(String[] args)
    {
        MyDI di = new MyDI(new TestConfig());
        IC c = di.getInstanceViaDI(IC.class);
        System.out.println(c.getA());
    }

    @Override
    public Object getImplByKey(Object key)
    {
        return null;
    }
}
