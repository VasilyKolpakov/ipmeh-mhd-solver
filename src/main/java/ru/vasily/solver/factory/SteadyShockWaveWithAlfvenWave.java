package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjects;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.ShockJump;
import ru.vasily.solver.Utils;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.LeftAlfvenWave;
import ru.vasily.solver.border.MixedBorderConditions;
import ru.vasily.solver.initialcond.*;
import ru.vasily.solver.utils.SteadyShockByKryukov;


import static java.lang.Math.sqrt;
import static ru.vasily.core.ArrayUtils.copy;
import static ru.vasily.solver.factory.IMHDSolverFactory.CALCULATION_CONSTANTS;
import static ru.vasily.solver.factory.IMHDSolverFactory.PHYSICAL_CONSTANTS;


public class SteadyShockWaveWithAlfvenWave implements ConditionsFactory
{

    public static final String SSW_CONDITIONS_DATA = "SSW_conditions_data";

    @Override
    public Conditions createConditions(DataObject data)
    {
        DataObject conditionsData = data.getObj(SSW_CONDITIONS_DATA);

        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        int k_x = conditionsData.getInt("k_x");
        int k_y = conditionsData.getInt("k_y");
        double absK = sqrt(k_x * k_x + k_y * k_y);

        double u = conditionsData.getDouble("u");
        double uShift = conditionsData.getDouble("u_shift");
        double v = conditionsData.getDouble("v");
        double absBL = conditionsData.getDouble("abs_b");

        double bX = absBL * (k_x / absK);
        double bY = absBL * (k_y / absK);

        double pressureRatio = conditionsData.getDouble("p_ratio");
        double machNumber = conditionsData.getDouble("mach");
        double gamma = physicalConstants.getDouble("gamma");
        // TODO hack
        ShockJump jump = createShockJumpWithShift(u, uShift, v, bX, bY, pressureRatio, machNumber, gamma);
        double[][][] initialVals = createInitialVals(data, jump);
        Array2dBorderConditions conditions = combineInOrder
                (
                        new MixedBorderConditions(
                                calculationConstants.getInt("xRes"),
                                calculationConstants.getInt("yRes"))
                        ,
                        new LeftAlfvenWave(calculationConstants,
                                           physicalConstants,
                                           jump.left, conditionsData)
                );
        System.out.println("SteadyShockWaveWithAlfvenWave.createConditions");
        System.out.format("shock jump = %s\n", jump);

        return new Conditions(conditions, initialVals);
    }

    private ShockJump createShockJumpWithShift(double u, double uShift, double v, double bX, double bY, double pressureRatio, double machNumber, double gamma)
    {
        final ShockJump jumpWithoutShift = SteadyShockByKryukov.steadyShockByKryukov2(u, v,
                                                                                      bX, bY,
                                                                                      machNumber,
                                                                                      pressureRatio,
                                                                                      gamma);

        MHDValues shiftedLeft = shiftMHDValues(jumpWithoutShift.left, uShift);
        MHDValues shiftedRight = shiftMHDValues(jumpWithoutShift.right, uShift);
        return new ShockJump(shiftedLeft, shiftedRight);
    }

    private MHDValues shiftMHDValues(MHDValues values, double uShift)
    {
        return MHDValues.builder()
                        .rho(values.rho)
                        .u(values.u + uShift)
                        .v(values.v)
                        .w(values.w)
                        .p(values.p)
                        .bX(values.bX)
                        .bY(values.bY)
                        .bZ(values.bZ)
                        .build();
    }

    private double[][][] createInitialVals(DataObject data, ShockJump jump)
    {
        DataObject calculationConstants = data.getObj(CALCULATION_CONSTANTS);
        DataObject physicalConstants = data.getObj(PHYSICAL_CONSTANTS);
        DataObject conditionData = data.getObj(SSW_CONDITIONS_DATA);
        double gamma = physicalConstants.getDouble("gamma");
        double x_s = conditionData.getDouble("x_s");
        System.out.println("SteadyShockWaveWithAlfvenWave.createInitialVals");
        double[] valsLeft = new double[8];
        jump.left.setToArray(valsLeft, gamma);
        double[] valsRight = new double[8];
        jump.right.setToArray(valsRight, gamma);
        System.out.println("speed of sound left = " + Utils.fastShockSpeed(valsLeft, jump.left.bX, gamma));
        System.out.println("speed of sound right = " + Utils.fastShockSpeed(valsRight, jump.right.bX, gamma));

        Init2dFunction function = new HorizontalShockFunction(jump.left,
                                                              jump.right,
                                                              x_s,
                                                              gamma);

        Init2dFunction disturbanceFunction = new LeftSideAlfvenDisturbanceFunction(conditionData,
                                                                                   physicalConstants,
                                                                                   calculationConstants,
                                                                                   jump.left);

        return getInitialConditions(calculationConstants,
                                    physicalConstants,
                                    function,
                                    disturbanceFunction
                                   );
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

}
