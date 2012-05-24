package ru.vasily.core.math;

public class Functions
{
    public static Function polynomial(final double... coefficients)
    {
        return new Function()
        {
            @Override
            public double value(double x)
            {
                double d = 0;
                double temp = x;
                for (double coefficient : coefficients)
                {
                    d += temp * coefficient;
                    temp *= x;
                }
                return d;
            }
        };
    }
}
