package ru.vasily.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class RealFileSystem implements FileSystem
{

	@Override
	public void write(CharSequence from, File to, Charset charset) throws IOException
	{
		Files.write(from, to, charset);
	}

	@Override
	public boolean isDirectory(File file)
	{
		return file.isDirectory();
	}

	@Override
	public boolean isFile(File file)
	{
		return file.isFile();
	}

	@Override
	public String toString(File file, Charset charset) throws IOException
	{
		return Files.toString(file, charset);
	}

	@Override
	public String getAbsolutePath(File file)
	{
		return file.getAbsolutePath();
	}

	@Override
	public File[] listFiles(File file)
	{
		return file.listFiles();
	}

	@Override
	public boolean exists(File file)
	{
		return file.exists();
	}

	@Override
	public void mkdir(File file)
	{
		file.mkdir();
	}

	@Override
	public void write(Writable handler, File to) throws IOException
	{

		Writer writer = null;
		try
		{
			writer = new FileWriter(to);
			handler.writeTo(writer);
		}
		finally
		{
			Closeables.closeQuietly(writer);
		}
	}

	@Override
	public File[] listFiles(File file, FilenameFilter filenameFilter)
	{
		return file.listFiles(filenameFilter);
	}

	@Override
	public <T> T parse(Parser<T> handler, File from) throws IOException
	{
		FileReader in = null;
		try
		{
			in = new FileReader(from);
			return handler.parseFrom(in);
		}
		finally
		{
			Closeables.closeQuietly(in);
		}
	}

	@Override
	public void writeQuietly(Writable writable, File to)
	{
		try
		{
			write(writable, to);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
