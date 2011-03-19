package ru.vasily.solverhelper.misc;

import java.io.IOException;
import java.io.Reader;

public interface ISerializer {
	<T> T readObject(Reader source, Class<T> clazz) throws IOException;

	void writeObject(Object obj, Appendable target);

	void writeObjects(Iterable<Object> obj, Appendable target);
}
