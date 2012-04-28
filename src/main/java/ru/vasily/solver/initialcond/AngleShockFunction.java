package ru.vasily.solver.initialcond;

import ru.vasily.solver.MHDValues;

import static java.lang.Math.*;
import static ru.vasily.solver.Utils.setConservativeValues;

public class AngleShockFunction implements Init2dFunction
{
    public static final double FLAT_ANGLE_IN_DEGREES = 180.0;
    private final MHDValues left;
    private final MHDValues right;
    private final double x_c;
    private final double gamma;

    /**
     * @param left
     * @param right
     * @param x_c
     * @param gamma
     */
    public AngleShockFunction(MHDValues left,
                              MHDValues right,
                              double x_c,
                              double gamma)
    {
        this.left = left;
        this.right = right;
        this.x_c = x_c;
        this.gamma = gamma;
    }

    @Override
    public void apply(double[] arr, double x, double y)
    {
        if (x < x_c)
        {
            setConservativeValues(left, arr, gamma);
        }
        else
        {
            setConservativeValues(right, arr, gamma);
        }
    }
//
//    private double rotate_x(double a_cos, double a_sin, double Ux, double Uy)
//    {
//        return Ux * a_cos - Uy * a_sin;
//    }
//
//    private double rotate_y(double a_cos, double a_sin, double Ux, double Uy)
//    {
//        return Ux * a_sin + Uy * a_cos;
//    }
//
//    private MHDValues rotate(MHDValues values, double a_cos, double a_sin)
//    {
//        return MHDValues.builder()
//                .rho(values.rho)
//                .p(values.p)
//                .u(rotate_x(a_cos, a_sin, values.u, values.v))
//                .v(rotate_y(a_cos, a_sin, values.u, values.v))
//                .w(values.w)
//                .bX(rotate_x(a_cos, a_sin, values.bX, values.bY))
//                .bY(rotate_y(a_cos, a_sin, values.bX, values.bY))
//                .bZ(values.bZ)
//                .build();
//    }

}
