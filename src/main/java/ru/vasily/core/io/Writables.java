package ru.vasily.core.io;

import java.io.IOException;

public class Writables
{
    public static String toString(Writable writable)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            writable.writeTo(sb);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static Writable asWritable(final String string)
    {
        return new Writable()
        {
            @Override
            public void writeTo(Appendable out) throws IOException
            {
                out.append(string);
            }
        };
    }
}
