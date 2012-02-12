package ru.vasily.dataobjs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import com.google.common.io.CharStreams;

import ru.vasily.core.io.Writable;

public class DataFile
{
    public static Writable createFile(final String title, final double[] xCoord, final Map<String, double[]> data)
    {
        if (data.isEmpty())
        {
            throw new IllegalArgumentException("no data");
        }
        return new Writable()
        {

            @Override
            public void writeTo(Appendable appendable) throws IOException
            {
                PrintWriter out = new PrintWriter(CharStreams.asWriter(appendable));
                Set<String> keySet = data.keySet();
                int length = xCoord.length;
                for (String key : keySet)
                {
                    if (length != data.get(key).length)
                    {
                        throw new IllegalArgumentException("lenghts are not equal");
                    }
                }
                out.println("TITLE=\"" + title + "\"");
                out.println("VARIABLES = " + variablesString(title, keySet));
                out.println("ZONE  I=" + length + " F=POINT");
                for (int i = 0; i < length; i++)
                {
                    out.print(xCoord[i]);
                    out.print("\t");
                    for (String key : keySet)
                    {
                        out.print(data.get(key)[i]);
                        out.print("\t");
                    }
                    out.println();
                }
            }

            private String variablesString(final String title, Set<String> valueNames)
            {
                StringBuilder varStr = new StringBuilder();
                varStr.append("\"X\"");
                for (String valName : valueNames)
                {
                    varStr.append(",\"");
                    varStr.append(valName);
                    varStr.append("\"");
                }
                return varStr.toString();
            }

        };
    }

}
