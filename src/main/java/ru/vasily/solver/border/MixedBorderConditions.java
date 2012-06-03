package ru.vasily.solver.border;

import static ru.vasily.core.ArrayUtils.copy;

public class MixedBorderConditions implements Array2dBorderConditions
{
    private final int xRes;
    private final int yRes;

    public MixedBorderConditions(int xRes, int yRes)
    {
        this.xRes = xRes;
        this.yRes = yRes;
    }

    @Override
    public void applyConditions(double[][][] values, double time)
    {
        // continuation
        for (int j = 0; j < yRes; j++)
        {
            copy(values[0][j], values[1][j]);
            copy(values[xRes - 1][j], values[xRes - 2][j]);
        }
        // periodic
        for (int i = 0; i < xRes; i++)
        {
            copy(values[i][0], values[i][yRes - 2]);
            copy(values[i][yRes - 1], values[i][1]);
        }
    }
}
