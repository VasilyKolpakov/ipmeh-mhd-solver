package ru.vasily.solver.initialcond;

import ru.vasily.application.misc.ArrayUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 9:08 PM
 * To change this template use File | Settings | File Templates.
 */
class FillSquareFunction implements Init2dFunction
{
    private final double[] val;
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public FillSquareFunction(double[] val, double x1, double y1, double x2, double y2)
    {
        this.val = val;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public void apply(double[] arr, double x, double y)
    {
        if (inOrder(x1, x, x2) && inOrder(y1, y, y2))
        {
            ArrayUtils.copy(arr, val);
        }
    }

    private boolean inOrder(double c1, double c2, double c3)
    {
        return c1 <= c2 && c2 <= c3;
    }
}
