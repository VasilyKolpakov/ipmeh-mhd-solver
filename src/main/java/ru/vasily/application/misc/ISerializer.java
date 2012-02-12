package ru.vasily.application.misc;

import ru.vasily.core.io.Writable;

public interface ISerializer
{
    Writable asWritable(Object obj);

}
