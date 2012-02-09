package ru.vasily.solver.utils;

import junit.framework.Assert;

import static java.lang.Math.*;

import org.junit.Test;
import ru.vasily.solver.initialcond.MagneticChargeSpotFunc;

public class MagneticChargeSpotFuncTest
{
    @Test
    public void zeroDivB()
    {
        MagneticChargeSpotFunc func = new MagneticChargeSpotFunc(0, 0, 1.01, 1);
        for (int i = 0; i < 1000; i++)
        {
            double alfa = 2 * PI / 1000 * i;
            Assert.assertEquals(1, divB(cos(alfa), sin(alfa), func), 0.0000001);
        }
    }

    private double divB(double x, double y, MagneticChargeSpotFunc func)
    {
        double delta = 0.000000001;
        return (bX(x + delta, y, func) - bX(x - delta, y, func)) / (delta * 2) +
                (bY(x, y + delta, func) - bY(x, y - delta, func)) / (delta * 2);
    }

    private double bX(double x, double y, MagneticChargeSpotFunc func)
    {
        double[] val = new double[8];
        func.apply(val, x, y);
        return val[5];
    }

    private double bY(double x, double y, MagneticChargeSpotFunc func)
    {
        double[] val = new double[8];
        func.apply(val, x, y);
        return val[6];
    }
}
