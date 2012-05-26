package ru.vasily.solver.initialcond;

import com.google.common.collect.ImmutableMap;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.ShockJump;
import ru.vasily.solver.Utils;
import ru.vasily.solver.utils.SteadyShockByKryukov;

import java.util.Map;

public class InitialConditionsFactories
{
    static final Map<String, Function2DFactory> functionFactories = ImmutableMap
            .<String, Function2DFactory>builder().
                    put("fill_rect", new FillRect()).
                    put("fill_circle", new FillCircle()).
                    put("magnetic_charge_spot", new MagneticChargeSpot()).
                    put("rotor_problem", new RotorProblem()).
                    put("orsag_tang_vortex", new OrsagTangVortex()).
                    put("kelvin_helmholtz", new KelvinHelmholtz()).
                    put("steady_shock", new SteadyShock()).
                    put("steady_shock_alt", new SteadyShockAlternative()).
                    build();

    public static class FillRect implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            double[] val = parseConservativeVals(data, physicalConstants);
            return new FillSquareFunction(val, data.getDouble("x1"),
                                          data.getDouble("y1"), data.getDouble("x2"),
                                          data.getDouble("y2"));
        }
    }

    public static class MagneticChargeSpot implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            final double xSpot = data.getDouble("x");
            final double ySpot = data.getDouble("y");
            final double spot_radius = data.getDouble("radius");
            double divB = data.getDouble("divB");
            return new MagneticChargeSpotFunc(xSpot, ySpot, spot_radius, divB);
        }
    }

    public static class FillCircle implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            double[] val = parseConservativeVals(data, physicalConstants);
            final double x = data.getDouble("x");
            final double y = data.getDouble("y");
            final double radius = data.getDouble("radius");
            return new FillCircleFunction(val, x, y, radius);
        }
    }

    public static class RotorProblem implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            return new RotorProblemFunction(data, physicalConstants.getDouble("gamma"));
        }
    }

    public static class OrsagTangVortex implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            return new OrsagTangVortexFunction(physicalConstants.getDouble("gamma"));
        }
    }

    public static class KelvinHelmholtz implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            return new KelvinHelmholtzFunction(data, physicalConstants.getDouble("gamma"));
        }
    }

    public static class SteadyShock implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject data, DataObject physicalConstants)
        {
            MHDValues leftValues = MHDValues.fromDataObject(data.getObj("leftValues"));
            double gamma = physicalConstants.getDouble("gamma");
            MHDValues rightValues = Utils.getSteadyShockRightValues(leftValues, gamma);
            return new HorizontalShockFunction(leftValues, rightValues, data.getDouble("x_s"), gamma);
        }
    }

    public static class SteadyShockAlternative implements Function2DFactory
    {

        @Override
        public Init2dFunction createFunction(DataObject conditionsData, DataObject physicalConstants)
        {
            double gamma = physicalConstants.getDouble("gamma");
            double absVelocityL = conditionsData.getDouble("abs_v");
            double velocityAngleL = conditionsData.getDouble("v_angle");
            double absBL = conditionsData.getDouble("abs_b");
            double BAngleL = conditionsData.getDouble("b_angle");
            double pressureRatio = conditionsData.getDouble("p_ratio");
            double machNumber = conditionsData.getDouble("mach");


            ShockJump jump = SteadyShockByKryukov.steadyShockByKryukov(absVelocityL,
                                                                       velocityAngleL,
                                                                       absBL,
                                                                       BAngleL,
                                                                       gamma,
                                                                       pressureRatio,
                                                                       machNumber);
            return new HorizontalShockFunction(jump.left, jump.right, conditionsData.getDouble("x_s"), gamma);
        }
    }


    private static double[] parseConservativeVals(DataObject data, DataObject physicalConstants)
    {
        double[] val = new double[8];
        Utils.setConservativeValues(data.getObj("value"), val,
                                    physicalConstants.getDouble("gamma"));
        return val;
    }

}
