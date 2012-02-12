package ru.vasily.solver.initialcond;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.Utils;

import static java.lang.Math.*;

/**
 * Created by IntelliJ IDEA.
 * User: gri
 * Date: 11/25/11
 * Time: 5:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class KelvinHelmholtzFunction implements Init2dFunction
{
    private final double gamma;
    private final double p_0;
    private final double B_0;
    private final double u_0;

    public KelvinHelmholtzFunction(DataObject params, double gamma)
    {
        this.gamma = gamma;
        p_0 = params.getDouble("p_0");
        B_0 = params.getDouble("B_0");
        u_0 = params.getDouble("u_0");
    }

    @Override
    public void apply(double[] arr, double x, double y)
    {
        MHDValues val = MHDValues.builder().
                p(p_0).
                rho(1).
                u(-u_0 * uFunc(y)).
                v(vFunc(x, y)).
                w(0).
                bX(B_0).bY(0).bZ(0).
                build();
        Utils.setCoservativeValues(val, arr, gamma);
    }

    private double uFunc(double y)
    {
        return tanh(20 * (y + 0.5)) - tanh(20 * (y - 0.5)) - 1.0;
    }

    private double vFunc(double x, double y)
    {
        double first__exp = exp(-100 * pow(y + 0.5, 2));
        double second_exp = exp(-100 * pow(y - 0.5, 2));
        return 0.25 * sin(2 * PI * x) * (first__exp - second_exp);
    }
}
