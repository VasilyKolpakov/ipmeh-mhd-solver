package ru.vasily.solver.utils;

import ru.vasily.core.math.Complex;
import ru.vasily.core.math.Function;
import ru.vasily.core.math.Functions;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.ShockJump;

import java.util.List;

import static java.lang.Math.*;
import static ru.vasily.core.math.ComplexMath.polynomial;
import static ru.vasily.core.math.ComplexMath.roots;
import static ru.vasily.core.math.EquationSolver.solveWithBisection;

public class SteadyShockByKryukov
{
    private static final double ONE_4PI_DIVIDE = 1.0 / (4 * PI);
    private static final double EPSILON = 0.000000000000001;


    public static ShockJump steadyShockByKryukov(double absVelocityL,
                                                 double velocityAngleL,
                                                 double absBL,
                                                 double BAngleL,
                                                 double gamma,
                                                 double pressureRatio,
                                                 double machNumber)
    {
        double u_xL = absVelocityL * cos(velocityAngleL);
        double u_yL = absVelocityL * sin(velocityAngleL);

        double m = sin(BAngleL);
        double l = cos(BAngleL);

        double m2 = m * m;
        double l2 = l * l;

        double B_xL = absBL * l;
        double B_yL = absBL * m;

        double Q2 = pressureRatio * pressureRatio;
        double Q4 = Q2 * Q2;

        double pL = ONE_4PI_DIVIDE * absBL * absBL / (gamma * Q2);

        double H = 1.0 + Q2;
        H = H + sqrt(H * H - 4.0 * Q2 * l2);

        double RhoL = 0.5 * gamma * pL * H * machNumber * machNumber / (u_xL * u_xL);

        double ML = machNumber * sqrt(0.5 * H);
        double ML2 = ML * ML;

        double a0 = -(gamma + 1.0) * square(ML2 - l2 * Q2);
        double a1 = (ML2 - l2 * Q2) * (2.0 + (gamma - 1.0) * ML2
                + gamma * Q2 * m2 - (gamma + 1.0) * Q2 * l2);
        double a2 = ((gamma - 1.0) * Q4 * l2 - (gamma - 2.0) * Q2 * ML2) * m2;
        double a3 = Q4 * l2 * m2;
        MHDValues left = MHDValues.builder()
                                  .rho(RhoL)
                                  .p(pL)
                                  .u(u_xL).v(u_yL).w(0)
                                  .bX(B_xL).bY(B_yL).bZ(0)
                                  .build();
        List<Complex> roots = roots(a0, a1, a2, a3);
        double xi = getRequiredRoot(roots);


        double R = (1.0 - l2 * (1.0 - xi) * Q2 / ML2) / xi;

        double RhoR = RhoL / R;

        double pR = gamma * pL * ((1.0 - R) * ML2 + 1.0 / gamma + 0.5 * (1.0 - xi * xi) * m2 * Q2);

        double u_xR = R * u_xL;
        double u_yR = u_yL - u_xL * (1.0 - xi) * Q2 * l * m / ML2;

        double B_xR = B_xL;
        double B_yR = xi * B_yL;
        MHDValues right = MHDValues.builder()
                                   .rho(RhoR)
                                   .p(pR)
                                   .u(u_xR).v(u_yR).w(0)
                                   .bX(B_xR).bY(B_yR).bZ(0)
                                   .build();
        return new ShockJump(right, left);
    }

    private static double square(double a)
    {
        return a * a;
    }

    private static double getRequiredRoot(List<Complex> numbers)
    {
        System.out.println("SteadyShockWaveWithDisturbance.getRequiredRoot roots = " + numbers);
        for (Complex number : numbers)
        {
            if (abs(number.im) < EPSILON && number.re >= 0.0 && number.re <= 1.0 + EPSILON)
            {
                return number.re;
            }
        }
        throw new RuntimeException("there are no appropriate roots in " + numbers);
    }

}

