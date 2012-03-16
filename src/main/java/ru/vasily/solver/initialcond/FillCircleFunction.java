package ru.vasily.solver.initialcond;

import com.google.common.base.Preconditions;
import ru.vasily.application.misc.ArrayUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 9:22 PM
 * To change this template use File | Settings | File Templates.
 */
class FillCircleFunction implements Init2dFunction
{
    private final double x;
    private final double y;
    private final double radius_squared;
    private final double[] val;

    public FillCircleFunction(double[] val, double x, double y, double radius)
    {
        Preconditions.checkArgument(radius > 0, "radius must be > 0, not %s", radius);
        this.x = x;
        this.y = y;
        this.val = val.clone();
        this.radius_squared = radius * radius;
    }

    @Override
    public void apply(double[] arr, double x, double y)
    {
        if (isInCircle(x, y))
        {
            ArrayUtils.copy(arr, val);
        }
    }

    private boolean isInCircle(double x, double y)
    {
        double dx = x - this.x;
        double dy = y - this.y;
        return (dx * dx + dy * dy) <= radius_squared;
    }
}
