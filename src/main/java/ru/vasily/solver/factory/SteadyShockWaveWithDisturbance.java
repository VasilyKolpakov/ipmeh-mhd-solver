package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjects;
import ru.vasily.core.math.Complex;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.LeftDisturbanceWave;
import ru.vasily.solver.initialcond.AngleShockFunction;
import ru.vasily.solver.initialcond.Array2dFiller;
import ru.vasily.solver.initialcond.Init2dFunction;
import ru.vasily.solver.initialcond.InitialValues2dBuilder;

import java.util.List;

import static ru.vasily.core.ArrayUtils.copy;
import static ru.vasily.solver.factory.IMHDSolverFactory.*;

import static java.lang.Math.*;
import static ru.vasily.core.math.ComplexMath.*;


public class SteadyShockWaveWithDisturbance implements ConditionsFactory
{

    private static final double EPSILON = 0.000000000000001;
    public static final String SSW_CONDITIONS_DATA = "SSW_conditions_data";

    @Override
    public Conditions createConditions(DataObject data)
    {
        DataObject conditionsData = data.getObj(SSW_CONDITIONS_DATA);
        double absVelocityL = conditionsData.getDouble("abs_v");
        double velocityAngleL = conditionsData.getDouble("v_angle");
        double absBL = conditionsData.getDouble("abs_b");
        double BAngleL = conditionsData.getDouble("b_angle");

        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        double gamma = physicalConstants.getDouble("gamma");

        double pressureRatio = conditionsData.getDouble("p_ratio");
        double machNumber = conditionsData.getDouble("mach");


        ShockJump jump = steadyShock(absVelocityL,
                velocityAngleL,
                absBL,
                BAngleL,
                gamma,
                pressureRatio,
                machNumber);
        double[][][] initialVals = createInitialVals(data, jump);
        Array2dBorderConditions conditions = combineInOrder
                (
                        new MixedBorderConditions(
                                calculationConstants.getInt("xRes"),
                                calculationConstants.getInt("yRes"))
                        ,
                        new LeftDisturbanceWave(calculationConstants,
                                physicalConstants,
                                jump.left, conditionsData)
                );
        System.out.println("SteadyShockWaveWithDisturbance.createConditions");
        System.out.format("shock jump = %s", jump);

        return new Conditions(conditions, initialVals);
    }

    private double[][][] createInitialVals(DataObject data, ShockJump jump)
    {
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject conditionData = data.getObj(SSW_CONDITIONS_DATA);
        double gamma = physicalConstants.getDouble("gamma");
        double x_c = conditionData.getDouble("x_c");

        Init2dFunction function = new AngleShockFunction(jump.left,
                jump.right,
                x_c,
                gamma);

        return getInitialConditions(calculationConstants,
                physicalConstants,
                function);
    }

    private ShockJump steadyShock(double absVelocityL,
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

        double pressureR = gamma * pressureL
                *
                (
                        (1.0 - R) * ml_2_pow
                                + 1.0 / gamma
                                + 0.5 * (1.0 - root * root) * BAngle_sin_2_pow * pressureRatio_2_pow
                );
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

    private double getRequiredRoot(List<Complex> numbers)
    {
        for (Complex number : numbers)
        {
            if (abs(number.im) < EPSILON && number.re >= 0.0 && number.re <= 1.0 + EPSILON)
            {
                return number.re;
            }
        }
        throw new RuntimeException("there are no appropriate roots in " + numbers);
    }

    private static class ShockJump
    {
        public final MHDValues left;
        public final MHDValues right;

        private ShockJump(MHDValues left, MHDValues right)
        {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString()
        {
            return "ShockJump{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    private double[][][] getInitialConditions(DataObject calculationConstants,
                                              DataObject physicalConstants,
                                              Init2dFunction function2D
                                             )
    {
        int xRes = calculationConstants.getInt("xRes");
        int yRes = calculationConstants.getInt("yRes");
        double xLength = physicalConstants.getDouble("xLength");
        double yLength = physicalConstants.getDouble("yLength");
        double x_0 = DataObjects.getDouble(physicalConstants, "x_0", 0.0);
        double y_0 = DataObjects.getDouble(physicalConstants, "y_0", 0.0);

        InitialValues2dBuilder<double[][][]> builder = new Array2dFiller(xRes,
                yRes,
                x_0,
                y_0,
                xLength,
                yLength);
        builder.apply(function2D);
//        builder.apply(disturbance);
        return builder.build();
    }

    private static class MixedBorderConditions implements Array2dBorderConditions
    {
        private final int xRes;
        private final int yRes;

        public MixedBorderConditions(int xRes, int yRes)
        {
            this.xRes = xRes;
            this.yRes = yRes;
        }

        @Override
        public void applyConditions(double[][][] values, double time)
        {
            // continuation
            for (int j = 0; j < yRes; j++)
            {
                copy(values[0][j], values[1][j]);
                copy(values[xRes - 1][j], values[xRes - 2][j]);
            }
            // periodic
            for (int i = 0; i < xRes; i++)
            {
                copy(values[i][0], values[i][yRes - 2]);
                copy(values[i][yRes - 1], values[i][1]);
            }
        }
    }

    private static class DisturbanceFunction implements Init2dFunction
    {
        private final double k_x;
        private final double k_y;
        private final double vAmp;
        private final double pAmp;
        private final double rhoAmp;
        private final double angle;

        private DisturbanceFunction(double angle, double waveLength, double vAmp, double pAmp, double rhoAmp)
        {
            this.angle = angle;
            k_x = cos(angle) / waveLength;
            k_y = sin(angle) / waveLength;

            this.vAmp = vAmp;
            this.pAmp = pAmp;
            this.rhoAmp = rhoAmp;
        }

        @Override
        public void apply(double[] arr, double x, double y)
        {
            double l = x * k_x + y * k_y;
            double l_2_PI_mul_mul_sin = sin(l * 2 * PI);
            arr[0] += l_2_PI_mul_mul_sin * rhoAmp;
            arr[1] += l_2_PI_mul_mul_sin * cos(angle) * vAmp;
            arr[2] += l_2_PI_mul_mul_sin * sin(angle) * vAmp;
            arr[3] += 0;
            arr[4] += l_2_PI_mul_mul_sin * pAmp;
        }
    }

    private Array2dBorderConditions combineInOrder(final Array2dBorderConditions conditions1,
                                                   final Array2dBorderConditions conditions2)
    {
        return new Array2dBorderConditions()
        {
            @Override
            public void applyConditions(double[][][] values, double time)
            {
                conditions1.applyConditions(values, time);
                conditions2.applyConditions(values, time);
            }
        };
    }
}
