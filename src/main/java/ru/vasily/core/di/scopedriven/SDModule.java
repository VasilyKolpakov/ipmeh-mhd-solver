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

}
