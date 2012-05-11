package ru.vasily.core.di.scopedriven;

import java.util.List;

public interface SDModule
{
    public static final SDModule EMPTY_MODULE = new SDModule()
    {
        @Override
        public SDComponent getComponentByName(String key)
        {
            return null;
        }
    };

    public SDComponent getComponentByName(String key);

    public interface SDComponentVisitor<T>
    {
        public T visitComplexComponent(ClassAndSDModule classAndSDModule);

        public T visitPrimitive(Object primitive);

        public T visitList(List<SDComponent> components);
    }

    public interface SDComponent
    {
        public <T> T accept(SDComponentVisitor<T> visitor);
    }

    class ComplexComponent implements SDComponent
    {
        private final ClassAndSDModule classAndSDModule;

        public ComplexComponent(Class clazz, SDModule module)
        {
            this.classAndSDModule = new ClassAndSDModule(clazz, module);
        }

        @Override
        public <T> T accept(SDComponentVisitor<T> visitor)
        {
            return visitor.visitComplexComponent(classAndSDModule);
        }
    }

    class Primitive implements SDComponent
    {
        private final Object object;

        public Primitive(Object object)
        {
            this.object = object;
        }

        @Override
        public <T> T accept(SDComponentVisitor<T> visitor)
        {
            return visitor.visitPrimitive(object);
        }
    }

    class ListComponent implements SDComponent
    {

        private final List<SDComponent> componentList;

        public ListComponent(List<SDComponent> componentList)
        {
            this.componentList = componentList;
        }

        @Override
        public <T> T accept(SDComponentVisitor<T> visitor)
        {
            return visitor.visitList(componentList);
        }
    }
}
