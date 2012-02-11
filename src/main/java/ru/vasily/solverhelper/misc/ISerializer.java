package ru.vasily.solverhelper.misc;

import ru.vasily.core.Writable;

import java.io.IOException;
import java.io.Reader;

public interface ISerializer
{
    Writable asWritable(Object obj);

}
