package ru.vasily.core.io;

import java.io.IOException;

public interface Writable
{
    void writeTo(Appendable out) throws IOException;
}
