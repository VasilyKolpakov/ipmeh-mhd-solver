package ru.vasily.core.di.mydi;

import ru.vasily.core.di.DIKey;

public class TestAnnotationKeyComponent
{
    public TestAnnotationKeyComponent(@DIKey("key") Object dependency)
    {
    }
}
