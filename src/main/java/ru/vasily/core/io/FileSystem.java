package ru.vasily.core.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public interface FileSystem
{
    public enum Permission
    {
        READ, WRITE, EXECUTE
    }

    boolean isDirectory(String path);

    boolean isFile(String path);

    String getAbsolutePath(String path);

    String getFileName(String path);

    String createPath(String parent, String... children);

    List<String> listDirContents(String path);

    boolean exists(String path);

    void mkdir(String path);

    Map<Permission, Boolean> getPermissions(String path);

    void setPermissions(String path, Map<Permission, Boolean> permissions);

    void write(Writable writable, String toPath) throws IOException;

    <T> T parse(String fromPath, Parser<T> parser) throws IOException;

}
