package ru.vasily.core.io;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.Reader;

final public class Parsers
{
    private Parsers()
    {
    }

    public static Parser<String> asString()
    {
        return new Parser<String>()
        {
            @Override
            public String parseFrom(Reader in) throws IOException
            {
                return CharStreams.toString(in);
            }
        };
    }
}
