package ru.vasily.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

public interface FileSystem {
	boolean isDirectory(File file);

	boolean isFile(File file);

	String toString(File file, Charset charset) throws IOException;

	String getAbsolutePath(File file);

	File[] listFiles(File file);

	boolean exists(File file);

	void mkdir(File file);

	void write(CharSequence from, File to, Charset charset) throws IOException;

	void write(Writable writable, File to) throws IOException;

	public interface Writable {
		void writeTo(Appendable out) throws IOException;
	}

	File[] listFiles(File file, FilenameFilter filenameFilter);

	<T> T read(Readable<T> readable, File from) throws IOException;

	public interface Readable<T> {
		T readFrom(Reader in) throws IOException;
	}
}
