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
        ImmutableList.Builder<SDModule.SDComponent> listBuilder = ImmutableList.builder();

        public ComponentListBuilder addPrimitive(Object object)
        {
            listBuilder.add(new SDModule.Primitive(object));
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

        public List<SDModule.SDComponent> build()
        {
            return listBuilder.build();
        }
    }
}
