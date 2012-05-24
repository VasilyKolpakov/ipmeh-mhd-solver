package ru.vasily.core.math;

import static java.lang.Math.cos;

public class EquationSolver
{
    public static double solveWithBisection(Function f, double a, double b)
    {
        if (!(f.value(a) > 0) ^ (f.value(b) > 0))
        {
            throw new IllegalArgumentException("bad function");
        }
        if (f.value(b) < f.value(a))
        {
            double temp = a;
            a = b;
            b = temp;
        }
        double ret = 0;
        for (int i = 0; i < 60; i++)
        {
            ret = (a + b) / 2;
            if (f.value(ret) > 0)
            {
                b = ret;
            }
            else
            {
                a = ret;
            }
        }
        return ret;
    }

    public static void main(String[] args)
    {
        Function f = new Function()
        {

            public double value(double x)
            {
                return cos(x);
            }
        };
        double a = 1;
        double b = -12;
        System.out.println((a > 0) ^ (b > 0));
        double y = solveWithBisection(f, 0, Math.PI);
        System.out.println(y);
        System.out.println(cos(y));
    }
}
