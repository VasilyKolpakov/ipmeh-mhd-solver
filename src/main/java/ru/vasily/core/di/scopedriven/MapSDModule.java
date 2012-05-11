package ru.vasily.core.di.scopedriven;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;

public class MapSDModule implements SDModule
{

    private final ImmutableMap<String, SDComponent> moduleConfig;

    private MapSDModule(ImmutableMap<String, SDComponent> moduleConfig)
    {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public SDComponent getComponentByName(String key)
    {
        return moduleConfig.get(key);
    }

    public static ModuleBuilder builder()
    {
        return new ModuleBuilder();
    }

    public static class ModuleBuilder
    {
        ImmutableMap.Builder<String, SDComponent> mapBuilder = ImmutableMap.builder();

        public ModuleBuilder putComplexComponent(String key, Class clazz, SDModule module)
        {
            ComplexComponent complexComponent = new ComplexComponent(clazz, module);
            mapBuilder.put(key, complexComponent);
            return this;
        }

        public ModuleBuilder putComplexComponent(String key, Class clazz)
        {
            ComplexComponent complexComponent = new ComplexComponent(clazz, EMPTY_MODULE);
            mapBuilder.put(key, complexComponent);
            return this;
        }

        public ModuleBuilder putPrimitive(String key, Object object)
        {
            Primitive primitive = new Primitive(object);
            mapBuilder.put(key, primitive);
            return this;
        }

        public ModuleBuilder putList(String key, List<SDComponent> componentList)
        {
            ListComponent listComponent = new ListComponent(componentList);
            mapBuilder.put(key, listComponent);
            return this;
        }

        public SDModule build()
        {
            return new MapSDModule(mapBuilder.build());
        }
    }
}
