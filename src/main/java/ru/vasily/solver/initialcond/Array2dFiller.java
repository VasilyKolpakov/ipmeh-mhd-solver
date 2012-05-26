package ru.vasily.solver.initialcond;

import ru.vasily.core.ArrayUtils;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjects;

import java.util.List;

public class Array2dFiller implements InitialValues2dBuilder<double[][][]>
{
    private final int xRes;
    private final int yRes;
    private final double x_0;
    private final double y_0;
    private final double xSize;
    private final double ySize;
    private final double[][][] array;

    public Array2dFiller(int xRes, int yRes, double x_0, double y_0, double xLength, double yLength)
    {
        this.xRes = xRes;
        this.yRes = yRes;
        this.x_0 = x_0;
        this.y_0 = y_0;
        this.xSize = xLength;
        this.ySize = yLength;
        array = new double[xRes][yRes][8];
    }

    @Override
    public void apply(Init2dFunction function)
    {
        for (int i = 0; i < xRes; i++)
        {
            for (int j = 0; j < yRes; j++)
            {
                double x = x_0 + xSize / (xRes - 1) * i;
                double y = y_0 + ySize / (yRes - 1) * j;
                function.apply(array[i][j], x, y);
            }
        }
    }

    @Override
    public double[][][] build()
    {
        return ArrayUtils.copy(array);
    }

    public static double[][][] parseInitialConditions(DataObject calculationConstants, DataObject physicalConstants, List<DataObject> initial_conditions_2d)
    {
        int xRes = calculationConstants.getInt("xRes");
        int yRes = calculationConstants.getInt("yRes");
        double xLength = physicalConstants.getDouble("xLength");
        double yLength = physicalConstants.getDouble("yLength");
        double x_0 = DataObjects.getDouble(physicalConstants, "x_0", 0.0);
        double y_0 = DataObjects.getDouble(physicalConstants, "y_0", 0.0);

        InitialValues2dBuilder<double[][][]> builder = new Array2dFiller(xRes, yRes, x_0, y_0, xLength,
                                                                         yLength);
        for (DataObject initData : initial_conditions_2d)
        {
            final String type = initData.getString("type");
            final Function2DFactory function2DFactory = InitialConditionsFactories.functionFactories.get(type);
            if (function2DFactory == null)
            {
                final String message =
                        String.format("intial condition function type '%s' is not supported, try %s",
                                      type, InitialConditionsFactories.functionFactories.keySet());
                throw new RuntimeException(message);
            }
            Init2dFunction function2D = function2DFactory
                    .createFunction(initData, physicalConstants);
            builder.apply(function2D);
        }
        return builder.build();
    }

}
