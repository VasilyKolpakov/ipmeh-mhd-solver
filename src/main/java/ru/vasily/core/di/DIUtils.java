package ru.vasily.core.di;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

public class DIUtils
{
    @Nullable
    public static String findKey(Annotation[] annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof DIKey)
            {
                return ((DIKey) annotation).value();
            }
        }
        return null;
    }
}
