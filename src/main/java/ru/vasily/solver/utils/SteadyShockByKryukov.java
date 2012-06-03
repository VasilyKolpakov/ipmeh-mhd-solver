package ru.vasily.solver.utils;

import ru.vasily.core.math.Complex;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.ShockJump;

import java.util.List;

import static java.lang.Math.*;
import static ru.vasily.core.math.ComplexMath.roots;

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

        double sinBAngle = sin(BAngleL);
        double cosBAngle = cos(BAngleL);

        double B_xL = absBL * cosBAngle;
        double B_yL = absBL * sinBAngle;

        return steadyShockByKryukovBase(u_xL, u_yL,
                                        absBL,
                                        sinBAngle, cosBAngle,
                                        B_xL, B_yL,
                                        machNumber,
                                        pressureRatio,
                                        gamma);
    }

    private static ShockJump steadyShockByKryukovBase(double u_xL, double u_yL, double absBL, double sinBAngle, double cosBAngle, double b_xL, double b_yL, double machNumber, double pressureRatio, double gamma)
    {
        double sinBAngle_square = sinBAngle * sinBAngle;
        double cosBAngle_square = cosBAngle * cosBAngle;

        double Q2 = pressureRatio * pressureRatio;
        double Q4 = Q2 * Q2;

        double pL = ONE_4PI_DIVIDE * absBL * absBL / (gamma * Q2);

        double H = 1.0 + Q2;
        H = H + sqrt(H * H - 4.0 * Q2 * cosBAngle_square);

        double RhoL = 0.5 * gamma * pL * H * machNumber * machNumber / (u_xL * u_xL);

        double ML = machNumber * sqrt(0.5 * H);
        double ML2 = ML * ML;

        double a0 = -(gamma + 1.0) * square(ML2 - cosBAngle_square * Q2);
        double a1 = (ML2 - cosBAngle_square * Q2) * (2.0 + (gamma - 1.0) * ML2
                + gamma * Q2 * sinBAngle_square - (gamma + 1.0) * Q2 * cosBAngle_square);
        double a2 = ((gamma - 1.0) * Q4 * cosBAngle_square - (gamma - 2.0) * Q2 * ML2) * sinBAngle_square;
        double a3 = Q4 * cosBAngle_square * sinBAngle_square;
        MHDValues left = MHDValues.builder()
                                  .rho(RhoL)
                                  .p(pL)
                                  .u(u_xL).v(u_yL).w(0)
                                  .bX(b_xL).bY(b_yL).bZ(0)
                                  .build();
        List<Complex> roots = roots(a0, a1, a2, a3);
        double xi = getRequiredRoot(roots);


        double R = (1.0 - cosBAngle_square * (1.0 - xi) * Q2 / ML2) / xi;

        double RhoR = RhoL / R;

        double pR = gamma * pL * ((1.0 - R) * ML2 + 1.0 / gamma + 0.5 * (1.0 - xi * xi) * sinBAngle_square * Q2);

        double u_xR = R * u_xL;
        double u_yR = u_yL - u_xL * (1.0 - xi) * Q2 * cosBAngle * sinBAngle / ML2;

        double B_xR = b_xL;
        double B_yR = xi * b_yL;
        MHDValues right = MHDValues.builder()
                                   .rho(RhoR)
                                   .p(pR)
                                   .u(u_xR).v(u_yR).w(0)
                                   .bX(B_xR).bY(B_yR).bZ(0)
                                   .build();
        return new ShockJump(right, left);
    }

    public static ShockJump steadyShockByKryukov2(double uL,
                                                  double vL,
                                                  double bX,
                                                  double bY,
                                                  double machNumber,
                                                  double pressureRatio,
                                                  double gamma)
    {

        double absBL_square = bX * bX + bY * bY;
        double sinBAngle_square = bY * bY / absBL_square;
        double cosBAngle_square = bX * bX / absBL_square;

        return steadyShockByKryukovBase(uL, vL,
                                        square(absBL_square),
                                        sqrt(sinBAngle_square), sqrt(cosBAngle_square),
                                        bX, bY,
                                        machNumber,
                                        pressureRatio,
                                        gamma);

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

