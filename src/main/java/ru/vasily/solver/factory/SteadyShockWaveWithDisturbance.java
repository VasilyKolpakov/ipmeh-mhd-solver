package ru.vasily.solver.factory;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjects;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.ShockJump;
import ru.vasily.solver.Utils;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.LeftDisturbanceWave;
import ru.vasily.solver.initialcond.*;

import java.util.Map;

import static ru.vasily.core.ArrayUtils.copy;
import static ru.vasily.solver.MHDValues.fromDataObject;
import static ru.vasily.solver.factory.IMHDSolverFactory.*;
import static ru.vasily.solver.utils.SteadyShockByKryukov.steadyShockByKryukov;


public class SteadyShockWaveWithDisturbance implements ConditionsFactory
{

    public static final String SSW_CONDITIONS_DATA = "SSW_conditions_data";
    private static final Map<String, ShockJumpFactory> shockJumpFactories =
            ImmutableMap.<String, ShockJumpFactory>builder()
                        .put("mhd", new ShockJumpWithMagneticFieldFactory())
                        .put("hydro", new HydrodynamicShockJumpFactory())
                        .build();

    @Override
    public Conditions createConditions(DataObject data)
    {
        DataObject conditionsData = data.getObj(SSW_CONDITIONS_DATA);

        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        final String type = conditionsData.getString("type");
        ShockJumpFactory shockJumpFactory = shockJumpFactories.get(type);
        if (shockJumpFactory == null)
        {
            final String message = String.format("illegal shockJumpFactory type '%s' only %s supported"
                    , type, shockJumpFactories.keySet());
            throw new RuntimeException(message);
        }
        ShockJump jump = shockJumpFactory.createShockJump(conditionsData, physicalConstants);
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
        System.out.format("shock jump = %s\n", jump);

        return new Conditions(conditions, initialVals);
    }

    private double[][][] createInitialVals(DataObject data, ShockJump jump)
    {
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject conditionData = data.getObj(SSW_CONDITIONS_DATA);
        double gamma = physicalConstants.getDouble("gamma");
        double x_c = conditionData.getDouble("x_s");

        System.out.println("SteadyShockWaveWithDisturbance.createInitialVals");
        double[] valsLeft = new double[8];
        jump.left.setToArray(valsLeft, gamma);
        double[] valsRight = new double[8];
        jump.right.setToArray(valsRight, gamma);
        System.out.println("speed of sound left = " + Utils.fastShockSpeed(valsLeft, jump.left.bX, gamma));
        System.out.println("speed of sound right = " + Utils.fastShockSpeed(valsRight, jump.right.bX, gamma));

        Init2dFunction function = new HorizontalShockFunction(jump.left,
                                                              jump.right,
                                                              x_c,
                                                              gamma);

        Init2dFunction disturbanceFunction = new LeftSideDisturbanceFunction(conditionData,
                                                                             physicalConstants,
                                                                             jump.left);

        return getInitialConditions(calculationConstants,
                                    physicalConstants,
                                    function,
                                    disturbanceFunction);
    }


    private double[][][] getInitialConditions(DataObject calculationConstants,
                                              DataObject physicalConstants,
                                              Init2dFunction... functions
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
        for (Init2dFunction function : functions)
        {
            builder.apply(function);
        }
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

    private Array2dBorderConditions combineInOrder(final Array2dBorderConditions... conditions)
    {
        return new Array2dBorderConditions()
        {
            @Override
            public void applyConditions(double[][][] values, double time)
            {
                for (Array2dBorderConditions condition : conditions)
                {
                    condition.applyConditions(values, time);
                }
            }
        };
    }

    private static class HydrodynamicShockJumpFactory implements ShockJumpFactory
    {
        @Override
        public ShockJump createShockJump(DataObject conditionsData, DataObject physicalConstants)
        {
            double gamma = physicalConstants.getDouble("gamma");
            MHDValues leftValues = fromDataObject(conditionsData.getObj("leftValues"));
            MHDValues rightValues = Utils.getSteadyShockRightValues(leftValues, gamma);
            return new ShockJump(leftValues, rightValues);
        }
    }

    private static class ShockJumpWithMagneticFieldFactory implements ShockJumpFactory
    {
        @Override
        public ShockJump createShockJump(DataObject conditionsData, DataObject physicalConstants)
        {
            double gamma = physicalConstants.getDouble("gamma");
            double absVelocityL = conditionsData.getDouble("abs_v");
            double velocityAngleL = conditionsData.getDouble("v_angle");
            double absBL = conditionsData.getDouble("abs_b");
            double BAngleL = conditionsData.getDouble("b_angle");
            double pressureRatio = conditionsData.getDouble("p_ratio");
            double machNumber = conditionsData.getDouble("mach");
            return steadyShockByKryukov(absVelocityL,
                                        velocityAngleL,
                                        absBL,
                                        BAngleL,
                                        gamma,
                                        pressureRatio,
                                        machNumber);
        }
    }

    public static interface ShockJumpFactory
    {
        ShockJump createShockJump(DataObject conditionsData, DataObject physicalConstants);
    }
}
