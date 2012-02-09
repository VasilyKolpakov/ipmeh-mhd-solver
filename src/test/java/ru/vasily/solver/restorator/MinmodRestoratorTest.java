package ru.vasily.solver.restorator;

import junit.framework.Assert;

import org.junit.Test;

public class MinmodRestoratorTest
{
    private MinmodRestorator restorator = new MinmodRestorator();

    @Test
    public void linear()
    {
        test(0.5, 0, 1, 2);
    }

    @Test
    public void non_linear_right()
    {
        test(1.5, 1, 2, 100);
    }

    @Test
    public void local_min_left()
    {
        test(1, 2, 1, 2);
    }

    private void test(double expected, double vLeft, double vRight, double vRightRight)
    {
        double restored = restorator.restore(vLeft, vRight, vRightRight);
        Assert.assertEquals(expected, restored, 0.0000001);
    }
}
