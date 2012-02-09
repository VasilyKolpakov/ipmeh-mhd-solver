package ru.vasily.solver.restorator;

import static java.lang.Math.*;

public class MinmodRestorator implements ThreePointRestorator
{
    @Override
    public double restore(double vLeft, double vRight, double vRightRight)
    {
        return vRight - minmodDividedByTwo(vRight - vLeft, vRightRight - vRight);
    }

    private double minmodDividedByTwo(double d1, double d2)
    {
        return (signum(d1) + signum(d2)) * (min(abs(d1), abs(d2))) * 0.25;
    }
}
