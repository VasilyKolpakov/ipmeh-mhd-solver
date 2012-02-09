package ru.vasily.solverhelper.tecplot;

import java.io.File;
import java.io.IOException;

public class TecplotManager implements ITecplotManager
{

    @Override
    public void runMacro(File macro) throws IOException
    {
        runTecplotMacroTask(macro).run();
    }

    @Override
    public void runMacro(Iterable<File> macro) throws IOException
    {
        for (File mcr : macro)
        {
            runMacro(mcr);
        }
    }

    private static Runnable runTecplotMacroTask(final File macro)
    {
        return new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    Runtime.getRuntime()
                            .exec("tecplot -p " + macro.getName(),
                                  (String[]) null,
                                  new File(macro.getParent())).waitFor();
                    Thread.sleep(1000);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
