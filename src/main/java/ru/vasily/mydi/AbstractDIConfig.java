package ru.vasily.mydi;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDIConfig implements DIConfig
{
    private Map<Class<?>, Object> classKeysToImpls = new HashMap<Class<?>, Object>();
    private Map<Object, Object> objectKeysToImpls = new HashMap<Object, Object>();

    public AbstractDIConfig()
    {
        initConfig();
    }

    public abstract void initConfig();

    public void addImplIgnoringInterface(Class<?> clazz)
    {
        registerComponent(clazz, clazz);
    }

    public void addImpl(Class<?> clazz)
    {
        registerComponent(clazz, clazz);
        for (Class<?> interf : clazz.getInterfaces())
        {
            registerComponent(interf, clazz);
        }
    }

    public void addObject(Object obj)
    {
        Class<? extends Object> clazz = obj.getClass();
        registerComponent(clazz, obj);
        for (Class<?> interf : clazz.getInterfaces())
        {
            registerComponent(interf, obj);
        }
    }

    public void registerComponent(Class<?> keyClass, Object impl)
    {
        if (classKeysToImpls.keySet().contains(keyClass))
        {
            throw new RuntimeException(
                    "Duplicate implementations for key class = "
                            + keyClass.getCanonicalName() + " impl = {"
                            + classKeysToImpls.get(keyClass).toString() + ", "
                            + impl.toString() + "}");
        }
        classKeysToImpls.put(keyClass, impl);
    }

    public void registerComponent(Object key, Object impl)
    {
        if (objectKeysToImpls.keySet().contains(key))
        {
            throw new RuntimeException(
                    "Duplicate objects for key = "
                            + key + " object = {"
                            + classKeysToImpls.get(key).toString() + ", object to register"
                            + impl.toString() + "}");
        }
        objectKeysToImpls.put(key, impl);
    }

    @Override
    public Object getImplByClass(Class<?> clazz)
    {
        return classKeysToImpls.get(clazz);
    }

    @Override
    public Object getImplByKey(Object key)
    {
        return objectKeysToImpls.get(key);
    }

}
