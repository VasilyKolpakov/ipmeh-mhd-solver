package ru.vasily.core.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public interface FileSystem
{
    boolean isDirectory(String path);

    boolean isFile(String path);

    String getAbsolutePath(String path);

    String getFileName(String path);

    String createPath(String parent, String... children);

    List<String> listDirContents(String path);

    boolean exists(String path);

    void mkdir(String path);

    //TODO vararg? Writable... writable
    void write(Writable writable, String toPath) throws IOException;

    void writeQuietly(Writable writable, String toPath);

    <T> T parse(String fromPath, Parser<T> parser) throws IOException;

}
