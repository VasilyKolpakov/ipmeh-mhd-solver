package ru.vasily.core.di.scopedriven;

import ru.vasily.core.di.DIKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vasily.core.di.DIUtils.findKey;

public class ScopeDrivenDI
{
    private final SDModule module;
    private final Map<String, Object> instances = new HashMap<String, Object>();
    private final ScopeDrivenDI parentContainer;
    private final ComponentVisitor componentVisitor = new ComponentVisitor();

    public ScopeDrivenDI(SDModule module)
    {
        this.module = module;
        this.parentContainer = new ScopeDrivenDI(null, null)
        {
            @Override
            public <T> T getInstance(Class<T> clazz)
            {
                return null;
            }

            @Override
            protected Object getInstance(String key)
            {
                return null;
            }
        };
    }

    public ScopeDrivenDI(ScopeDrivenDI parentContatiner, SDModule module)
    {
        this.parentContainer = parentContatiner;
        this.module = module;
    }

    public <T> T getInstance(Class<T> clazz)
    {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        checkArgument(constructors.length == 1, "class %s must have one and only one constructor", clazz);
        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameters.length; i++)
        {
            String key = findKey(parameterAnnotations[i]);
            checkNotNull(key, "all parameters of %s constructor must be annotated with %s", clazz, DIKey.class);
            parameters[i] = getInstance(key);
        }
        try
        {
            return (T) constructor.newInstance(parameters);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Object getInstance(String key)
    {
        final Object instance;
        if (instances.get(key) != null)
        {
            instance = instances.get(key);
        }
        else if (parentContainer.getInstance(key) != null)
        {
            instance = parentContainer.getInstance(key);
        }
        else
        {
            instance = module.visitComponentByName(key, componentVisitor);
            instances.put(key, instance);
        }
        return instance;
    }

    private ScopeDrivenDI createChildDI(SDModule module)
    {
        return new ScopeDrivenDI(this, module);
    }

    private class ComponentVisitor implements SDModule.SDComponentVisitor<Object>
    {

        @Override
        public Object visitComplexComponent(ClassAndSDModule classAndSDModule)
        {
            ScopeDrivenDI child = createChildDI(classAndSDModule.module);
            return child.getInstance(classAndSDModule.clazz);
        }

        @Override
        public Object visitPrimitive(Object primitive)
        {
            return primitive;
        }
    }
}
