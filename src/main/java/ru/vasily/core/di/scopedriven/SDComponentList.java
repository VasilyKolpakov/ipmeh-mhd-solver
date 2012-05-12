package ru.vasily.core.di.scopedriven;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SDComponentList
{

    public static ComponentListBuilder listBuilder()
    {
        return new ComponentListBuilder();
    }

    public static class ComponentListBuilder
    {
        ImmutableList.Builder<SDComponent> listBuilder = ImmutableList.builder();

        public ComponentListBuilder addPrimitive(Object object)
        {
            listBuilder.add(new SDComponent.Primitive(object));
            return this;
        }

        public ComponentListBuilder addPrimitives(List<?> objects)
        {
            for (Object object : objects)
            {
                addPrimitive(object);
            }
            return this;
        }

        public ComponentListBuilder addComplexComponent(Class clazz, SDModule module)
        {
            SDComponent complexComponent = new SDComponent.ComplexComponent(clazz, module);
            listBuilder.add(complexComponent);
            return this;
        }

        public ComponentListBuilder addComplexComponent(Class clazz)
        {
            SDComponent complexComponent = new SDComponent.ComplexComponent(clazz, SDModule.EMPTY_MODULE);
            listBuilder.add(complexComponent);
            return this;
        }

        public List<SDComponent> build()
        {
            return listBuilder.build();
        }
    }
}
