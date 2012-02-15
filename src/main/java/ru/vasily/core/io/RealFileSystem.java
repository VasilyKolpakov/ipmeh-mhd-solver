package ru.vasily.core.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;

public class RealFileSystem implements FileSystem
{
    private final static Function<File, String> fileToPath = new Function<File, String>()
    {
        @Override
        public String apply(@Nullable File input)
        {
            checkArgument(input != null, "file is null");
            return input.getPath();
        }
    };
    private static final String PATH_SEPARATOR = System.getProperty("file.separator");

    @Override
    public boolean isDirectory(String path)
    {
        return new File(path).isDirectory();
    }

    @Override
    public boolean isFile(String path)
    {
        return new File(path).isFile();
    }

    @Override
    public String getAbsolutePath(String path)
    {
        return new File(path).getAbsolutePath();
    }

    @Override
    public String getFileName(String path)
    {
        return new File(path).getName();
    }

    @Override
    public String createPath(String parent, String... children)
    {
        return Joiner.on(PATH_SEPARATOR).join(Lists.asList(parent, children));
    }

    @Override
    public List<String> listDirContents(String path)
    {
        List<File> files = asList(new File(path).listFiles());
        return Lists.transform(files, fileToPath);
    }

    @Override
    public boolean exists(String path)
    {
        return new File(path).exists();
    }

    @Override
    public void mkdir(String path)
    {
        new File(path).mkdir();
    }

    @Override
    public Map<Permission, Boolean> getPermissions(String path)
    {
        File file = new File(path);
        return ImmutableMap.of(
                Permission.READ, file.canRead(),
                Permission.WRITE, file.canWrite(),
                Permission.EXECUTE, file.canExecute()
        );
    }

    @Override
    public void setPermissions(String path, Map<Permission, Boolean> permissions)
    {
        File file = new File(path);
        for (Map.Entry<Permission, Boolean> permission : permissions.entrySet())
        {
            switch (permission.getKey())
            {
                case READ:
                    file.setReadable(permission.getValue());
                    break;
                case WRITE:
                    file.setWritable(permission.getValue());
                    break;
                case EXECUTE:
                    file.setExecutable(permission.getValue());
                    break;
                default:
                    throw new RuntimeException(permission.getKey() + " not supported");
            }
        }
    }

    @Override
    public void write(Writable handler, String toPath) throws IOException
    {

        Writer writer = null;
        try
        {
            writer = new FileWriter(new File(toPath));
            handler.writeTo(writer);
        }
        finally
        {
            Closeables.closeQuietly(writer);
        }
    }

    @Override
    public <T> T parse(String fromPath, Parser<T> handler) throws IOException
    {
        FileReader in = null;
        try
        {
            in = new FileReader(new File(fromPath));
            return handler.parseFrom(in);
        }
        finally
        {
            Closeables.closeQuietly(in);
        }
    }
}
