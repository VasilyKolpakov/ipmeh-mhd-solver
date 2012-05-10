package ru.vasily.core.di.scopedriven;

import ru.vasily.core.di.CyclicDependencyFoundException;
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
    private ThreadLocalCyclicDependencyGuard cyclicDependencyGuard = new ThreadLocalCyclicDependencyGuard();

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
            checkNotNull(key, "all parameters of %s constructor must be annotated with %s", clazz, DIKey.class
                    .getCanonicalName());
            Object instance = getInstance(key);
            checkNotNull(instance, "%s component is not found for %s", key, clazz.getCanonicalName());
            parameters[i] = instance;
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
        else
        {
            SDModule.Component component = module.getComponentByName(key);
            if (component != null)
            {
                instance = component.accept(componentVisitor(key));
                instances.put(key, instance);
            }
            else
            {
                instance = parentContainer.getInstance(key);
            }
        }
        return instance;
    }

    private ScopeDrivenDI createChildDI(SDModule module)
    {
        return new ScopeDrivenDI(this, module);
    }


    private ComponentVisitor componentVisitor(String componentKey)
    {
        return new ComponentVisitor(componentKey);
    }

    private class ComponentVisitor implements SDModule.SDComponentVisitor<Object>
    {
        private final String key;

        private ComponentVisitor(String key)
        {
            this.key = key;
        }

        @Override
        public Object visitComplexComponent(ClassAndSDModule classAndSDModule)
        {
            try
            {
                cyclicDependencyGuard
                        .assertNotTrackedAndTrackComponent(key, classAndSDModule.clazz, ScopeDrivenDI.this);
                ScopeDrivenDI child = createChildDI(classAndSDModule.module);
                return child.getInstance(classAndSDModule.clazz);
            }
            catch (CyclicDependencyFoundException e)
            {
                throw e;
            }
            finally
            {
                cyclicDependencyGuard.untrackComponent(key, classAndSDModule.clazz, ScopeDrivenDI.this);
            }
        }

        @Override
        public Object visitPrimitive(Object primitive)
        {
            return primitive;
        }
    }
}
