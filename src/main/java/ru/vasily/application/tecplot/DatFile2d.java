package ru.vasily.application.tecplot;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.common.io.CharStreams;

import ru.vasily.core.io.Writable;

import static ru.vasily.core.ArrayUtils.*;

public class DatFile2d implements Writable
{

    private final double[][] y;
    private final double[][] x;
    private final String valueName;
    private final String title;
    private final double[][] value;

    public DatFile2d(String title, String valueName, double[][] x, double[][] y, double[][] value)
    {
        this.title = title;
        this.valueName = valueName;
        this.x = x;
        this.y = y;
        this.value = value;
    }

    @Override
    public void writeTo(Appendable appendable) throws IOException
    {
        PrintWriter out = new PrintWriter(CharStreams.asWriter(appendable));
        int xRes = x.length;
        int yRes = x[0].length;
        assertSquareArrays(xRes, yRes, x, y, value);
        out.print("TITLE=\"" + title + "\"\n");
        out.print("VARIABLES = \"x\", \"y\",\"" + valueName + "\"\n");
        out.print(" ZONE I = " + yRes + " J= " + xRes + " F=POINT\n");
        for (int i = 0; i < xRes; i++)
        {
            for (int j = 0; j < yRes; j++)
            {
                out.print(x[i][j]);
                out.print('\t');
                out.print(y[i][j]);
                out.print('\t');
                out.print(value[i][j]);
                out.print('\n');
            }
        }
    }
}
