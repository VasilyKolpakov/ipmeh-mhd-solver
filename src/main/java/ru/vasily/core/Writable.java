package ru.vasily.core;

import java.io.IOException;

public interface Writable
{
    void writeTo(Appendable out) throws IOException;
}
