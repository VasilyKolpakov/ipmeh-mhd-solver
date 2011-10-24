package ru.vasily.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

public interface FileSystem
{
	boolean isDirectory(File file);

	boolean isFile(File file);

	String toString(File file, Charset charset) throws IOException;

	String getAbsolutePath(File file);

	File[] listFiles(File file);

	boolean exists(File file);

	void mkdir(File file);

	void write(CharSequence from, File to, Charset charset) throws IOException;

	void write(Writable writable, File to) throws IOException;

	void writeQuietly(Writable writable, File to);

	List<File> listFiles(File file, FilenameFilter filenameFilter);

	<T> T parse(Parser<T> parser, File from) throws IOException;

}
