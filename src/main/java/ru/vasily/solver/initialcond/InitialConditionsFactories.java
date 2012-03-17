package ru.vasily.solver.initialcond;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.Utils;

public class InitialConditionsFactories
{
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

    private static double[] parseConservativeVals(DataObject data, DataObject physicalConstants)
    {
        double[] val = new double[8];
        Utils.setCoservativeValues(data.getObj("value"), val,
                physicalConstants.getDouble("gamma"));
        return val;
    }

}
