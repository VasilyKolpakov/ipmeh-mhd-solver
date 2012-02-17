package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;

import ru.vasily.core.parallel.ParallelEngine;
import ru.vasily.core.parallel.ParallelManager;
import ru.vasily.core.parallel.SmartParallelTask;

import static ru.vasily.core.collection.Reducers.*;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.restorator.MinmodRestorator;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RiemannSolver2D;
import ru.vasily.solver.utils.AllInOneMHDSolver2DReporter;
import ru.vasily.solver.utils.MHDSolver2DReporter;
import ru.vasily.solver.utils.Restorator2dUtility;
import ru.vasily.application.plotdata.PlotData;

import static java.lang.Math.*;
import static ru.vasily.solver.Utils.maximumFastShockSpeed;
import static ru.vasily.solver.Utils.toPhysical;
import static ru.vasily.application.misc.ArrayUtils.copy;

public class MHDSolver2D implements MHDSolver
{
    private int stepCount = 0;

    private double totalTime = 0;
    private final double[][][] predictorData;
    private final double[][][] correctorData;
    private final double[][][] left_right_flow;
    private final double[][][] up_down_flow;
    private final double[][] divB;
    private final double x_0;
    private final double y_0;

    @Override
    public double getTotalTime()
    {
        return totalTime;
    }

    private final int xRes;
    private final int yRes;
    private final double gamma;
    private final double hx;
    private final double hy;
    private final double CFL;
    private final MHDSolver2DReporter reporter;

    private final Array2dBorderConditions borderConditions;

    private RestoredFlowCalculator flowCalculator;

    private final ParallelEngine parallelEngine;

    public MHDSolver2D(DataObject params, ThreePointRestorator rawRestorator,
                       RiemannSolver2D riemannSolver, Array2dBorderConditions borderConditions,
                       ParallelEngine parallelEngine, double[][][] initialValues)
    {
        this.parallelEngine = parallelEngine;
        DataObject calculationConstants = params.getObj("calculationConstants");
        DataObject physicalConstants = params.getObj("physicalConstants");
        xRes = calculationConstants.getInt("xRes");
        yRes = calculationConstants.getInt("yRes");
        x_0 = getDouble(physicalConstants, "x_0", 0.0);
        y_0 = getDouble(physicalConstants, "y_0", 0.0);
        double xLength = physicalConstants.getDouble("xLength");
        double yLength = physicalConstants.getDouble("yLength");
        hx = xLength / (xRes - 1);
        hy = yLength / (yRes - 1);
        gamma = physicalConstants.getDouble("gamma");
        CFL = calculationConstants.getDouble("CFL");
        predictorData = copy(initialValues);
        correctorData = copy(initialValues);
        left_right_flow = new double[xRes][yRes][8];
        up_down_flow = new double[xRes][yRes][8];
        divB = new double[xRes][yRes];
        this.reporter = new AllInOneMHDSolver2DReporter();
        this.borderConditions = borderConditions;
        flowCalculator = new RestoredFlowCalculator(riemannSolver, rawRestorator, gamma);
    }

    private double getDouble(DataObject data, String valueName, double default_)
    {
        if (data.has(valueName))
        {
            return data.getDouble(valueName);
        }
        else
        {
            return default_;
        }
    }

    private final SmartParallelTask nextStepTask = new SmartParallelTask()
    {

        @Override
        public void doTask(ParallelManager par)
        {
            nextTimeStep(par);
        }
    };

    @Override
    public void nextTimeStep()
    {
        parallelEngine.run(nextStepTask);
    }

    private void nextTimeStep(ParallelManager par)
    {
        double tau = par.accumulate(minimum(), getTau(par));
        applyBorderConditions(par, predictorData);
        flowCalculator.calculateFlow(par, left_right_flow, up_down_flow, predictorData);

        applyFlow(par, tau, correctorData);

        applyBorderConditions(par, correctorData);

        flowCalculator.calculateFlow(par, left_right_flow, up_down_flow, correctorData);

        if (par.isMainThread())
        {
            average(predictorData, predictorData, correctorData);
        }
        applyFlow(par, tau / 2, predictorData);

        calculateDivB(par, predictorData);
        applyMagneticChargeFlow(par, tau, predictorData);
        applyBorderConditions(par, predictorData);

        if (par.isMainThread())
        {
            copy(correctorData, predictorData);
            stepCount++;
            totalTime += tau;
        }
    }

    private void applyBorderConditions(ParallelManager par, double[][][] predictorData2)
    {
        if (par.isMainThread())
        {
            borderConditions.applyConditions(predictorData2);
        }
    }

    private void average(double[][][] result, double[][][] sourceA, double[][][] sourceB)
    {
        for (int i = 0; i < result.length; i++)
        {
            for (int j = 0; j < result[0].length; j++)
            {
                for (int k = 0; k < result[0][0].length; k++)
                {
                    result[i][j][k] = (sourceA[i][j][k] + sourceB[i][j][k]) / 2;
                }
            }
        }
    }

    private void calculateDivB(ParallelManager par, double[][][] consVals)
    {
        Restorator2dUtility restorator = new Restorator2dUtility(new MinmodRestorator(), consVals,
                                                                 gamma);
        double[] temp = new double[8];
        for (int i : par.range(2, xRes - 1, true))
        {
            for (int j = 2; j < yRes - 1; j++)
            {
                double bY_cell_top = restorator.restoreDown(temp, i, j)[6];
                double bY_cell_bottom = restorator.restoreUp(temp, i, j - 1)[6];
                double bX_cell_left_side =
                        restorator.restoreRight(temp, i - 1, j)[5];
                double bX_cell_right_side =
                        restorator.restoreLeft(temp, i, j)[5];
                divB[i][j] = (bY_cell_top - bY_cell_bottom) / hy +
                        (bX_cell_right_side - bX_cell_left_side) / hx;
            }
        }
    }

    private void applyMagneticChargeFlow(ParallelManager par, double tau, double[][][] consVals)
    {
        for (int i : par.range(1, xRes - 1, true))
        {
            for (int j = 1; j < yRes - 1; j++)
            {
                double[] val = consVals[i][j];
                double ro = val[0];
                double roU = val[1];
                double roV = val[2];
                double roW = val[3];
                double U = roU / ro;
                double V = roV / ro;
                double W = roW / ro;
                double bX = val[5];
                double bY = val[6];
                double bZ = val[7];
                double divB_tau = divB[i][j] * tau;
                double pi_4 = PI * 4;
                val[1] -= bX / pi_4 * divB_tau;
                val[2] -= bY / pi_4 * divB_tau;
                val[3] -= bZ / pi_4 * divB_tau;
                val[4] -= (U * bX + V * bY + W * bZ) / pi_4 * divB_tau;
                val[5] -= U * divB_tau;
                val[6] -= V / pi_4 * divB_tau;
                val[7] -= W / pi_4 * divB_tau;
            }
        }
    }

    private double getTau(ParallelManager par)
    {
        double tau = Double.POSITIVE_INFINITY;
        double[] u_phy = new double[8];
        double lengthCharacteristic = min(hx, hy);
        for (int i : par.range(0, xRes, true))
        {
            for (int j = 0; j < yRes; j++)
            {
                toPhysical(u_phy, predictorData[i][j], gamma);
                double U = u_phy[1];
                double V = u_phy[2];
                double speedOfFlow = sqrt(U * U + V * V);
                double currentSpeed = speedOfFlow + maximumFastShockSpeed(u_phy, gamma);
                tau = min(lengthCharacteristic / currentSpeed, tau);
            }
        }
        return tau * CFL;
    }

    private void applyFlow(ParallelManager par, double timeStep, double[][][] consVal)
    {
        for (int i : par.range(2, xRes - 2, true))
        {
            for (int j = 2; j < yRes - 2; j++)
            {
                for (int k = 0; k < 8; k++)
                {
                    final double up_down_diff = up_down_flow[i][j - 1][k]
                            - up_down_flow[i][j][k];
                    double d = up_down_diff * timeStep / hy;
                    consVal[i][j][k] += d;

                    final double left_right_diff = left_right_flow[i - 1][j][k]
                            - left_right_flow[i][j][k];
                    consVal[i][j][k] += left_right_diff
                            * timeStep / hx;
                }
            }
        }
    }

    @Override
    public ImmutableMap<String, Object> getLogData()
    {
        return ImmutableMap.<String, Object>builder()
                .put("step count", stepCount)
                .put("total time", totalTime)
                .build();
    }

    @Override
    public PlotData getData()
    {
        PlotData plotData = reporter.report(xCoordinates(), yCoordinates(), predictorData, divB, up_down_flow,
                                            left_right_flow,                                            gamma);
        return plotData;
    }

    private double[][] xCoordinates()
    {
        double[][] x = new double[xRes][yRes];
        for (int i = 0; i < xRes; i++)
        {
            for (int j = 0; j < yRes; j++)
            {
                x[i][j] = x_0 + i * hx;
            }
        }
        return x;
    }

    private double[][] yCoordinates()
    {
        double[][] y = new double[xRes][yRes];
        for (int i = 0; i < xRes; i++)
        {
            for (int j = 0; j < yRes; j++)
            {
                y[i][j] = y_0 + j * hy;
            }
        }
        return y;
    }

}
