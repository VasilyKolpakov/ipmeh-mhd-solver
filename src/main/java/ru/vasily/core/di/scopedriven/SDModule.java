package ru.vasily.core.di.scopedriven;

public interface SDModule
{
    public static final SDModule EMPTY_MODULE = new SDModule()
    {
        @Override
        public Component getComponentByName(String key)
        {
            return null;
        }
    };

    public Component getComponentByName(String key);

    public interface SDComponentVisitor<T>
    {
        public T visitComplexComponent(ClassAndSDModule classAndSDModule);

        public T visitPrimitive(Object primitive);
    }

    public interface Component
    {
        public <T> T accept(SDComponentVisitor<T> visitor);
    }

    class ComplexComponent implements Component
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

    class Primitive implements Component
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
}
