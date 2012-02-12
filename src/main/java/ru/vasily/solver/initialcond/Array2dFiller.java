package ru.vasily.solver.initialcond;

import ru.vasily.application.misc.ArrayUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Array2dFiller implements InitialValues2dBuilder<double[][][]>
{
    private final int xRes;
    private final int yRes;
    private final double x_0;
    private final double y_0;
    private final double xSize;
    private final double ySize;
    private final double[][][] array;

    public Array2dFiller(int xRes, int yRes, double x_0, double y_0, double xLength, double yLength)
    {
        this.xRes = xRes;
        this.yRes = yRes;
        this.x_0 = x_0;
        this.y_0 = y_0;
        this.xSize = xLength;
        this.ySize = yLength;
        array = new double[xRes][yRes][8];
    }

    @Override
    public void apply(Init2dFunction function)
    {
        for (int i = 0; i < xRes; i++)
        {
            for (int j = 0; j < yRes; j++)
            {
                double x = x_0 + xSize / (xRes - 1) * i;
                double y = y_0 + ySize / (yRes - 1) * j;
                function.apply(array[i][j], x, y);
            }
        }
    }

    @Override
    public double[][][] build()
    {
        return ArrayUtils.copy(array);
    }
}
