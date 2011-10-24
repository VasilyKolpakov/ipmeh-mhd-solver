package ru.vasily.core;

import java.io.IOException;
import java.io.Reader;

public interface Parser<T>
{
	T parseFrom(Reader in) throws IOException;
}
