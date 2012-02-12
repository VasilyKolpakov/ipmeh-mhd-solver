package ru.vasily.solver.factory;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solver.MHDSolver1D;
import ru.vasily.solver.Utils;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RoeSolverByKryukov;

public class MHDSolver1DFactory implements IMHDSolverFactory
{

    private final RestoratorFactory restoratorFactory;

    public MHDSolver1DFactory(RestoratorFactory factory)
    {
        this.restoratorFactory = factory;
    }

    @Override
    public MHDSolver createSolver(DataObject params)
    {
        return new MHDSolver1D(params, restorator(params), new RoeSolverByKryukov(),
                               initialValues1d(params));
    }

    private ThreePointRestorator restorator(DataObject params)
    {
        DataObject restoratorData = params.getObj("restorator");
        String type = restoratorData.getString("type");
        return restoratorFactory.createRestorator(type);
    }

    private double[][] initialValues1d(DataObject params)
    {
        DataObject calculationConstants = params.getObj("calculationConstants");
        DataObject physicalConstants = params.getObj("physicalConstants");
        int xRes = calculationConstants.getInt("xRes");
        double gamma = physicalConstants.getDouble("gamma");
        double[][] initVals = new double[xRes][8];
        double xLength = physicalConstants
                .getDouble("xLength");
        for (DataObject initData : params.getObjects("initial_conditions_1d"))
        {
            int begin = (int) (xRes * initData.getDouble("begin") / xLength);
            int end = (int) (xRes * initData.getDouble("end") / xLength);
            for (int i = begin; i < end; i++)
            {
                double[] u = initVals[i];
                Utils.setCoservativeValues(initData.getObj("value"), u, gamma);
            }
        }
        return initVals;
    }

}
