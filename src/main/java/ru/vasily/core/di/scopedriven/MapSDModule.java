package ru.vasily.core.di.scopedriven;

import com.google.common.collect.ImmutableMap;

public class MapSDModule implements SDModule
{

    private final ImmutableMap<String, Component> moduleConfig;

    private MapSDModule(ImmutableMap<String, Component> moduleConfig)
    {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public Component getComponentByName(String key)
    {
        return moduleConfig.get(key);
    }

    public static ModuleBuilder builder()
    {
        return new ModuleBuilder();
    }

    public static class ModuleBuilder
    {
        ImmutableMap.Builder<String, Component> mapBuilder = ImmutableMap.builder();

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

        public SDModule build()
        {
            return new MapSDModule(mapBuilder.build());
        }
    }
}
