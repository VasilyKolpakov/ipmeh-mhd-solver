package ru.vasily.solver;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ru.vasily.solverhelper.plotdata.PlotDataVisitor;

import static java.lang.Math.abs;

@Deprecated
public class SymmetryChecker180 implements PlotDataVisitor
{
    private static Map<String, SymmChecker> checkers =
            ImmutableMap.<String, SymmChecker>builder().
                    put("u_2d", new Counter()).
                    put("v_2d", new Counter()).
                    put("w_2d", new Direct()).
                    put("abs_speed_2d", new Direct()).
                    put("magnetic_pressure_2d", new Direct()).
                    put("pressure_2d", new Direct()).
                    put("density_2d", new Direct()).
                    build();

    @Override
    public void process1D(String name, double[] x, double[] y)
    {
    }

    @Override
    public void process2D(String name, double[][] x, double[][] y, double[][] val)
    {
        SymmChecker checker = checkers.get(name);
        if (checker == null)
        {
            System.out.printf("symmetry check is not supported for %s\n", name);
            return;
        }
        int xRes = val.length;
        int yRes = val[0].length;
        for (int i = 2; i < xRes - 2; i++)
        {
            for (int j = 2; j < yRes - 2; j++)
            {
                if (!checker.check(val[i][j], val[xRes - 1 - i][yRes - 1 - j]))
                {
                    System.out.printf("symmetry voilated for %s , i = %d, j = %d\n", name, i, j);
                    return;
                }
            }
        }
        System.out.printf("symmetry NOT voilated for %s \n", name);
    }

    private interface SymmChecker
    {
        boolean check(double a, double b);
    }

    private static class Direct implements SymmChecker
    {

        @Override
        public boolean check(double a, double b)
        {
            return tolerantEquals(a, b);
        }
    }

    private static class Counter implements SymmChecker
    {

        @Override
        public boolean check(double a, double b)
        {
            return tolerantEquals(a, -b);
        }
    }

    private static boolean tolerantEquals(double a, double b)
    {
        return abs(a - b) < 0.0000001;
    }
}
