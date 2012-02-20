package ru.vasily.solver;

import ru.vasily.core.parallel.ParallelManager;
import ru.vasily.solver.restorator.MinmodRestorator;
import ru.vasily.solver.utils.Restorator2dUtility;

import static java.lang.Math.PI;

public class MagneticChargeFlowCalculator
{

    private final double[][][] magneticChargeFlow;
    private final int xRes;
    private final int yRes;
    private final double gamma;
    private final double hx;
    private final double hy;

    public MagneticChargeFlowCalculator(int xRes, int yRes, double hx, double hy, double gamma)
    {
        this.xRes = xRes;
        this.yRes = yRes;
        this.hx = hx;
        this.hy = hy;
        this.gamma = gamma;
        this.magneticChargeFlow = new double[xRes][yRes][8];
    }

    public void calculateFlow(ParallelManager par, double[][][] consVals)
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
                double divB = (bY_cell_top - bY_cell_bottom) / hy +
                        (bX_cell_right_side - bX_cell_left_side) / hx;


                double[] val = consVals[i][j];
                double[] flow = magneticChargeFlow[i][j];
                setFlow(val, divB, flow);
            }
        }
    }

    private void setFlow(double[] val, double divB, double[] flow)
    {
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
        double pi_4 = PI * 4;
        flow[0] = 0;
        flow[1] = -bX / pi_4 * divB;
        flow[2] = -bY / pi_4 * divB;
        flow[3] = -bZ / pi_4 * divB;
        flow[4] = -(U * bX + V * bY + W * bZ) / pi_4 * divB;
        flow[5] = -U * divB;
        flow[6] = -V * divB;
        flow[7] = -W * divB;
    }

    public void applyFlow(ParallelManager par, double tau, double[][][] consVals)
    {
        for (int i : par.range(1, xRes - 1, true))
        {
            for (int j = 1; j < yRes - 1; j++)
            {
                for (int k = 0; k < 8; k++)
                {
                    consVals[i][j][k] += magneticChargeFlow[i][j][k] * tau;
                }
            }
        }
    }
}
