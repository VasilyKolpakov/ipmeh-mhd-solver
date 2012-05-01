package ru.vasily.application.tecplot;

import java.io.File;
import java.io.IOException;

import ru.vasily.core.di.mydi.MyDI;
import ru.vasily.application.AppConfig;
import ru.vasily.application.misc.DirWalker;
import ru.vasily.application.misc.FileTypeFilter;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class MacroRunner
{
    private final ITecplotManager tecplotManager;

    public MacroRunner(ITecplotManager tecplotManager)
    {
        this.tecplotManager = tecplotManager;
    }

    public void runMacro(File output)
    {
        new DirWalker(new Function<File, Void>()
        {

            @Override
            public Void apply(File input)
            {
                try
                {
                    tecplotManager.runMacro(input);
                    input.deleteOnExit();
                } catch (IOException e)
                {
                    throw Throwables.propagate(e);
                }
                return null;
            }
        }, FileTypeFilter.forFileType("mcr")).walkDirs(output);
    }

    public static void main(String[] args)
    {
        MacroRunner app = new MyDI(new AppConfig())
                .getInstanceViaDI(MacroRunner.class);
        app.runMacro(new File(args[0]));
    }
}
