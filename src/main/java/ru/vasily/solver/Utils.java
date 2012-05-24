package ru.vasily.solver;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.math.Complex;
import ru.vasily.core.math.Function;
import ru.vasily.core.math.Vector2;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.*;
import static ru.vasily.core.math.ComplexMath.roots;
import static ru.vasily.core.math.EquationSolver.solveWithBisection;
import static ru.vasily.core.math.Functions.polynomial;
import static ru.vasily.solver.MHDValues.fromDataObject;

public final class Utils
{
    private static final double PI_SQUARED = PI * PI;
    private static final double EPSILON = 0.000000000000001;


    private Utils()
    {
    }

    public static final int iBX = 5;
    public static final int iBY = 6;
    public static final int iBZ = 7;

    public static double getPressure(double[] u, double gamma)
    {
        double ro = u[0];
        double roU = u[1];
        double roV = u[2];
        double roW = u[3];
        double e = u[4];
        double bX = u[5];
        double bY = u[6];
        double bZ = u[7];
        double p = (e
                - (roU * roU + roV * roV + roW * roW) / ro
                / 2 - (bX * bX + bY * bY + bZ * bZ) / 8
                / Math.PI)
                * (gamma - 1);
        return p;
    }

    public static double[] toPhysical(double[] result, double[] u, double gamma)
    {
        double ro = u[0];
        double roU = u[1];
        double roV = u[2];
        double roW = u[3];

        double bX = u[5];
        double bY = u[6];
        double bZ = u[7];
        double Rho = ro;

        double U = roU / ro;
        double V = roV / ro;
        double W = roW / ro;
        double PGas = getPressure(u, gamma);
        double BX = bX;
        double BY = bY;
        double BZ = bZ;
        result[0] = Rho;
        result[1] = U;
        result[2] = V;
        result[3] = W;
        result[4] = PGas;
        result[5] = BX;
        result[6] = BY;
        result[7] = BZ;
        return result;
    }

    /**
     * Kulikovskij_MatematVoprosiChislenResheniyaGiperbol.djvu page 338
     *
     * @param u_phy
     * @param bN    normal field component
     * @param gamma
     * @return
     */
    public static double fastShockSpeed(double[] u_phy, double bN, double gamma)
    {
        double ro = u_phy[0];
        // double U = u_phy[1];
        // double V = u_phy[2];
        // double W = u_phy[3];
        double PGas = u_phy[4];
        double bX = u_phy[5];
        double bY = u_phy[6];
        double bZ = u_phy[7];

        double b_square_div4piRo = (bX * bX + bY * bY + bZ * bZ)
                / (4 * PI * ro);
        double speedOfSound_square = gamma * PGas / ro;
        double speedOfSound = sqrt(speedOfSound_square);
        double absBx = abs(bN);
        double third = absBx * speedOfSound / sqrt(PI * ro);
        double cf = 0.5 *
                (
                        sqrt(speedOfSound_square + b_square_div4piRo + third) +
                                sqrt(speedOfSound_square + b_square_div4piRo - third)
                );
        return cf;
    }

    public static double maximumFastShockSpeed(double[] u_phy, double gamma)
    {
        double ro = u_phy[0];
        // double U = u_phy[1];
        // double V = u_phy[2];
        // double W = u_phy[3];
        double PGas = u_phy[4];
        double bX = u_phy[5];
        double bY = u_phy[6];
        double bZ = u_phy[7];
        double b_square_div4piRo = (bX * bX + bY * bY + bZ * bZ)
                / (4 * PI * ro);
        double speedOfSound_square = gamma * PGas / ro;
        return sqrt(speedOfSound_square + b_square_div4piRo);
    }

    private static Map<String, Integer> valueNumbers = ImmutableMap.<String, Integer>builder()
                                                                   .put("rho", 0)
                                                                   .put("u", 1)
                                                                   .put("v", 2)
                                                                   .put("w", 3)
                                                                   .put("p", 4)
                                                                   .put("bx", 5)
                                                                   .put("by", 6)
                                                                   .put("bz", 7)
                                                                   .build();

    public static int valueNumber(String valueName)
    {
        Integer integer = valueNumbers.get(valueName.toLowerCase());
        Preconditions.checkNotNull(integer, "illegal value name \'%s\'", valueName);
        return integer;
    }

    public static void setConservativeValues(DataObject data, double[] u, double gamma)
    {
        setConservativeValues(fromDataObject(data), u, gamma);
    }

    public static void setConservativeValues(MHDValues values, double[] u_val, double gamma)
    {
        double rho = values.rho;
        double p = values.p;
        double u = values.u;
        double v = values.v;
        double w = values.w;
        double bX = values.bX;
        double bY = values.bY;
        double bZ = values.bZ;
        u_val[0] = rho;
        u_val[1] = rho * u;
        u_val[2] = rho * v;
        u_val[3] = rho * w;
        u_val[4] = p / (gamma - 1) + rho * (u * u + v * v + w * w) / 2
                + (bY * bY + bZ * bZ + bX * bX) / 8 / PI;
        u_val[5] = bX;
        u_val[6] = bY;
        u_val[7] = bZ;
    }

    public static MHDValues getSteadyShockRightValues(MHDValues leftValues, double gamma)
    {
        checkArgument(leftValues.u > 0, "leftValues.u must be greater than zero");
        double m = leftValues.rho * leftValues.u;
        Vector2 Bt = new Vector2(leftValues.bY, leftValues.bZ);
        Vector2 Vt = new Vector2(leftValues.v, leftValues.w);
        final double m_square = m * m;
        double a1 = leftValues.p + m_square / leftValues.rho + 1.0 / (8 * PI) * Bt.square();
        double bX = leftValues.bX;
        Vector2 minus_Bt_Bx_4PI_divide_multiply = Bt.multiply(-bX / (4 * PI));
        Vector2 a2 = Vt.multiply(m).add(minus_Bt_Bx_4PI_divide_multiply);

        final double Cp = gamma / (gamma - 1);
        double energyPerMass =
                leftValues.p * Cp
                        + m_square / 2 / leftValues.rho
                        + Vt.square() / 2 * leftValues.rho
                        + Bt.square() / (4 * PI);
        double a3 = energyPerMass * m / leftValues.rho
                - bX * Bt.dot(Vt) / (4 * PI);
        Vector2 a4 = Vt.multiply(bX).add(Bt.multiply(-m / leftValues.rho));

        final double bX_square = bX * bX;
        final double a2_square = a2.square();
        double b1 = a2.multiply(bX).add(a4.multiply(-m)).square();
        double b2 = 1.0 / (8 * PI) * (2 - Cp);
        double b3 = 1.0 / (2 * m) * bX_square / (16 * PI_SQUARED);

        final double bX_quad = bX_square * bX_square;
        final double energyFlowValue = a2_square / (2 * m_square) - a3 / m;
        double zeroPowerCoefficient = -b1 * b3
                + bX_quad / (16 * PI_SQUARED) * m * energyFlowValue;
        double firstPowerCoefficient = m * (
                b1 * b2
                        - m_square * bX_square / (2 * PI) * energyFlowValue
                        + bX_quad / (16 * PI_SQUARED) * a1 * Cp
        );
        final double m_quad = m_square * m_square;
        double secondPowerCoefficient = m * (
                m_quad * energyFlowValue
                        - m_square * bX_square / (2 * PI) * a1 * Cp
                        + m_square * bX_quad / (16 * PI_SQUARED) * (0.5 - Cp)
        );
        final double m_fifth = m_quad * m;
        double thirdPowerCoefficient = m_fifth * (
                a1 * Cp - bX_square / (2 * PI) * (0.5 - Cp)
        );
        double fourthPowerCoefficient = m_fifth * m_square * (0.5 - Cp);
        Function poly = polynomial(
                zeroPowerCoefficient,
                firstPowerCoefficient,
                secondPowerCoefficient,
                thirdPowerCoefficient,
                fourthPowerCoefficient
                                  );
        double invertedRho = solveWithBisection(poly, 0, 1 / leftValues.rho);
        System.out.println("Utils.getSteadyShockRightValues poly(invertedRho) = "
                + poly.value(invertedRho));
        System.out.println("Utils.getSteadyShockRightValues Bt = " + Bt);
        System.out.println("Utils.getSteadyShockRightValues Vt = " + Bt);
        System.out.println("Utils.getSteadyShockRightValues a2.multiply(bX) = " + a2.multiply(bX));
        System.out.println("Utils.getSteadyShockRightValues a2.multiply(bX) = " + a2.multiply(bX));
        System.out.println("Utils.getSteadyShockRightValues b1 = " + b1);
        double rightRho = 1 / invertedRho;
        double rightU = m / rightRho;
        double energy_coeff = m_square / rightRho - bX_square / (4 * PI);
        Vector2 rightBt = a2.multiply(bX).add(a4.multiply(-m)).multiply(-1 / energy_coeff);
        Vector2 rightVt = a2.add(Bt.multiply(bX / (4 * PI))).multiply(1 / m);
        double rightP = a1 - m_square / rightRho - 1 / (8 * PI) * rightBt.square();

        return MHDValues.builder()
                        .p(rightP)
                        .rho(rightRho)
                        .u(rightU)
                        .v(rightVt.x1)
                        .w(rightVt.x2)
                        .bX(bX)
                        .bY(rightBt.x1)
                        .bZ(rightBt.x2)
                        .build();
    }

    public static ShockJump steadyShockByKryukov(double absVelocityL,
                                                 double velocityAngleL,
                                                 double absBL,
                                                 double BAngleL,
                                                 double gamma,
                                                 double pressureRatio,
                                                 double machNumber)
    {
        double velocityXL = cos(velocityAngleL) * absVelocityL;
        double velocityYL = sin(velocityAngleL) * absVelocityL;
        double BXL = cos(BAngleL) * absBL;
        double BYL = sin(BAngleL) * absBL;

        double pressureRatio_2_pow = pressureRatio * pressureRatio;
        double pressureRatio_4_pow = pressureRatio_2_pow * pressureRatio_2_pow;

        // m2
        double BAngle_sin_2_pow = sin(BAngleL) * sin(BAngleL);
        // l2
        double BAngle_cos_2_pow = cos(BAngleL) * cos(BAngleL);

        double pressureL = 1.0 / (4 * PI) * absBL * absBL / (gamma * pressureRatio_2_pow);

        double h0 = 1.0 + pressureRatio_2_pow;
        double H = h0 + sqrt(h0 * h0 - 4.0 * pressureRatio_2_pow * BAngle_cos_2_pow);

        double densityL =
                0.5 * gamma * pressureL * H * machNumber * machNumber
                        /
                        (velocityXL * velocityXL);

        double ml = machNumber * sqrt(0.5 * H);
        double ml_2_pow = ml * ml;

        double a00 = ml_2_pow - BAngle_cos_2_pow * pressureRatio_2_pow;
        double a0 = -(gamma + 1.0) * a00 * a00;
        double a1 = (ml_2_pow - BAngle_cos_2_pow * pressureRatio_2_pow)
                *
                (
                        2.0
                                + (gamma - 1.0) * ml_2_pow
                                + gamma * pressureRatio_2_pow * BAngle_sin_2_pow
                                - (gamma + 1.0) * pressureRatio_2_pow * BAngle_cos_2_pow
                );
        double a2 =
                (
                        (gamma - 1.0) * pressureRatio_4_pow * BAngle_cos_2_pow
                                - (gamma - 2.0) * pressureRatio_2_pow * ml_2_pow
                ) * BAngle_sin_2_pow;
        double a3 = pressureRatio_4_pow * BAngle_cos_2_pow * BAngle_sin_2_pow;
        List<Complex> roots = roots(a0, a1, a2, a3);
        double root = getRequiredRoot(roots);

        double R = (1.0 - BAngle_cos_2_pow * (1.0 - root) * pressureRatio_2_pow / ml_2_pow) / root;

        double densityR = densityL / R;

        double mult = (1.0 - R) * ml_2_pow
                + 1.0 / gamma
                + 0.5 * (1.0 - root * root) * BAngle_sin_2_pow * pressureRatio_2_pow;
        System.out.println("SteadyShockWaveWithDisturbance.steadyShockByKryukov mult = " + mult);
        System.out
              .println("SteadyShockWaveWithDisturbance.steadyShockByKryukov (1.0 - R) * ml_2_pow = " + ((1.0 - R) * ml_2_pow));
        System.out.println("SteadyShockWaveWithDisturbance.steadyShockByKryukov 1.0 / gamma = " + (1.0 / gamma));
        System.out
              .println("SteadyShockWaveWithDisturbance.steadyShockByKryukov third = " + (0.5 * (1.0 - root * root) * BAngle_sin_2_pow * pressureRatio_2_pow));
        double pressureR = gamma * pressureL
                *
                mult;
        double velocityXR = R * velocityXL;
        double velocityYR = velocityYL
                -
                velocityXL * (1.0 - root) * pressureRatio_2_pow
                        * cos(BAngleL) * sin(BAngleL) / ml_2_pow;

        double BXR = BXL;
        double BYR = root * BYL;
        MHDValues left = MHDValues.builder()
                                  .u(velocityXL).v(velocityYL).w(0)
                                  .p(pressureL).rho(densityL)
                                  .bX(BXL).bY(BYL).bZ(0)
                                  .build();
        MHDValues right = MHDValues.builder()
                                   .u(velocityXR).v(velocityYR).w(0)
                                   .p(pressureR).rho(densityR)
                                   .bX(BXR).bY(BYR).bZ(0)
                                   .build();
        return new ShockJump(left, right);
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
