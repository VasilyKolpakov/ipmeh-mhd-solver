package ru.vasily.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public interface FileSystem
{
    boolean isDirectory(String path);

    boolean isFile(String path);

    String toString(String path, Charset charset) throws IOException;

    String getAbsolutePath(String path);

    String getFileName(String path);

    String createPath(String parent, String... children);

    List<String> listDirContents(String path);

    boolean exists(String path);

    void mkdir(String path);

    void write(CharSequence from, String toPath, Charset charset) throws IOException;

    void write(Writable writable, String toPath) throws IOException;

    void writeQuietly(Writable writable, String toPath);

    List<String> listFiles(String path, FilenameFilter filenameFilter);

    <T> T parse(Parser<T> parser, String fromPath) throws IOException;

}
