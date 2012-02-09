package ru.vasily.solverhelper.misc;

import java.io.File;
import java.io.FileFilter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class DirWalker
{
    private final Function<File, Void> fileProcessor;
    private final FileFilter fileFilter;

    public DirWalker(Function<File, Void> fileProcessor)
    {
        this.fileProcessor = fileProcessor;
        this.fileFilter = null;
    }

    public DirWalker(Function<File, Void> fileProcessor,
                     FileFilter filenameFilter)
    {
        this.fileProcessor = fileProcessor;
        this.fileFilter = filenameFilter;
    }

    public void walkDirs(File dir)
    {
        Preconditions.checkArgument(dir.isDirectory());
        for (File file : dir.listFiles())
        {
            if (file.isDirectory())
            {
                walkDirs(file);
            }
            else if (file.isFile() && fileFilter.accept(file))
            {
                fileProcessor.apply(file);
            }
        }
    }
}
