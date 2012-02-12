package ru.vasily.solverhelper.misc;

import ru.vasily.core.io.Writable;

public interface ISerializer
{
    Writable asWritable(Object obj);

}
