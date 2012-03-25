package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.math.Complex;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.ContinuationBCF;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static ru.vasily.core.dataobjs.DataObjects.getDouble;
import static ru.vasily.core.dataobjs.DataObjects.asDataObj;
import static ru.vasily.solver.MHDValues.asDataObj;
import static ru.vasily.solver.factory.IMHDSolverFactory.*;

import static java.lang.Math.*;
import static ru.vasily.core.math.ComplexMath.*;
import static ru.vasily.solver.initialcond.Array2dFiller.FILL_RECT;
import static ru.vasily.solver.initialcond.Array2dFiller.parseInitialConditions;


public class SteadyShockWaveWithDisturbance implements ConditionsFactory
{

    public static final double EPSILON = 0.00000001;

    @Override
    public Conditions createConditions(DataObject data)
    {
        Array2dBorderConditions conditions = new ContinuationBCF().createConditions(data);
        DataObject conditionsData = data.getObj("SSW_conditions_data");
        double absVelocityL = conditionsData.getDouble("abs_v");
        double velocityAngleL = conditionsData.getDouble("v_angle");
        double absBL = conditionsData.getDouble("abs_b");
        double BAngleL = conditionsData.getDouble("b_angle");

        double gamma = data.getObj(PHYSICAL_CONSTANTS).getDouble("gamma");

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
        return new Conditions(conditions, initialVals);
    }

    private double[][][] createInitialVals(DataObject data, ShockJump jump)
    {
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        double xLength = physicalConstants.getDouble("xLength");
        double yLength = physicalConstants.getDouble("yLength");
        double x_0 = getDouble(physicalConstants, "x_0", 0.0);
        double y_0 = getDouble(physicalConstants, "y_0", 0.0);


        double middle = x_0 + xLength / 2;
        Map<String, Object> leftValues = ImmutableMap.<String, Object>builder()
                .put("type", FILL_RECT)
                .put("x1", x_0)
                .put("x2", middle)
                .put("y1", y_0)
                .put("y2", y_0 + yLength)
                .put("value", asDataObj(jump.left))
                .build();
        Map<String, Object> rightValues = ImmutableMap.<String, Object>builder()
                .put("type", FILL_RECT)
                .put("x1", middle)
                .put("x2", x_0 + xLength)
                .put("y1", y_0)
                .put("y2", y_0 + yLength)
                .put("value", asDataObj(jump.right))
                .build();

        List<DataObject> initial_conditions_2d = asList
                (
                        asDataObj(leftValues),
                        asDataObj(rightValues)
                );
        return parseInitialConditions(calculationConstants,
                                      physicalConstants,
                                      initial_conditions_2d);
    }

    private ShockJump steadyShock(double absVelocityL,
                                  double velocityAngleL,
                                  double absBL,
                                  double BAngleL,
                                  double gamma,
                                  double pressureRatio,
                                  double machNumber)
    {
        double velocityXL = sin(velocityAngleL) * absVelocityL;
        double velocityYL = cos(velocityAngleL) * absVelocityL;
        double BXL = sin(BAngleL) * absBL;
        double BYL = cos(BAngleL) * absBL;

        double machNumber_2_pow = machNumber * machNumber;
        double pressureRatio_2_pow = pressureRatio * pressureRatio;
        double pressureRatio_4_pow = pressureRatio_2_pow * pressureRatio_2_pow;
        double pressureL = 1.0 / (4 * PI) * absBL * absBL / (gamma * pressureRatio_2_pow);

        double h0 = 1.0 + pressureRatio_2_pow;
        double h = h0 + sqrt(h0 * h0 - 4.0 * pressureRatio_2_pow);

        double densityL =
                0.5 * gamma * pressureL * h * machNumber
                        /
                        (velocityXL * velocityXL);

        double ml = machNumber * sqrt(0.5 * h);
        double ml_2_pow = ml * ml;

        double BAngle_sin_2_pow = sin(BAngleL) * sin(BAngleL);
        double BAngle_cos_2_pow = cos(BAngleL) * cos(BAngleL);

        double a = pressureRatio_4_pow * BAngle_cos_2_pow * BAngle_sin_2_pow;
        double b =
                (
                        (gamma - 1.0) * machNumber_2_pow * BAngle_cos_2_pow
                                - (gamma - 2.0) * pressureRatio_2_pow * ml_2_pow
                ) * BAngle_sin_2_pow;
        double c = (ml_2_pow - BAngle_cos_2_pow * pressureRatio_2_pow)
                *
                (
                        2.0
                                + (gamma - 1.0) * ml_2_pow
                                + gamma * pressureRatio_2_pow * BAngle_sin_2_pow
                                - (gamma - 1.0) * pressureRatio_2_pow * BAngle_cos_2_pow
                );
        double d = -(gamma + 1.0) * (ml_2_pow - BAngle_cos_2_pow * BAngle_sin_2_pow) * (ml_2_pow - BAngle_cos_2_pow * BAngle_sin_2_pow);
        List<Complex> roots = roots(a, b, c, d);
        double root = getRequiredRoot(roots);

        double R = (1.0 - 2 * (1.0 - root) * pressureRatio_2_pow / ml_2_pow) / root;

        double densityR = densityL / R;

        double pressureR = gamma * pressureL
                *
                (
                        (1.0 - R) * ml_2_pow
                                + 1.0 / gamma
                                + 0.5 * (1.0 - root * root) * BAngle_sin_2_pow * pressureRatio_2_pow
                );
        double velocityXR = R * velocityXL;
        double velocityYR =
                (
                        velocityYL
                                - velocityXL * (1.0 - root) * pressureRatio_2_pow * cos(BAngleL) * sin(BAngleL)
                ) / ml_2_pow;

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
            if (abs(number.im) < EPSILON && number.re > 0.0 && number.re < 1.0)
            {
                return number.re;
            }
        }
        throw new RuntimeException("there are no appropriate roots in " + numbers);
    }

    private class ShockJump
    {
        public final MHDValues left;
        public final MHDValues right;

        private ShockJump(MHDValues left, MHDValues right)
        {
            this.left = left;
            this.right = right;
        }
    }
}
