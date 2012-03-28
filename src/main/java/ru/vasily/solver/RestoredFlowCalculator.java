package ru.vasily.solver;

import static ru.vasily.core.ArrayUtils.isNAN;
import static ru.vasily.solver.Utils.toPhysical;

import ru.vasily.core.parallel.ParallelManager;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RiemannSolver2D;
import ru.vasily.solver.utils.Restorator2dUtility;

public class RestoredFlowCalculator implements FlowCalculatorArray2D
{

    private final double gamma;
    private final RiemannSolver2D riemannSolver2D;
    private ThreePointRestorator rawRestorator;

    public RestoredFlowCalculator(RiemannSolver2D riemannSolver2D, ThreePointRestorator restorator,
                                  double gamma)
    {
        this.riemannSolver2D = riemannSolver2D;
        this.rawRestorator = restorator;
        this.gamma = gamma;
    }

    private void setFlow(double[] flow, double[] uL, double[] uR, int i, int j,     // TODO refactor to conservative values
                         double cos_alfa, double sin_alfa)
    {
        riemannSolver2D.getFlow(flow, uL, uR, gamma, gamma, cos_alfa, sin_alfa);
        if (isNAN(flow))
        {
            throw AlgorithmError.builder()
                    .put("error type", "NAN value in flow")
                    .put("i", i).put("j", j)
                    .put("left input", uL).put("right input", uR)
                    .put("output", flow)
                    .put("cos_alfa", cos_alfa).put("sin_alfa", sin_alfa)
                    .build();
        }
    }

    @Override
    public void calculateFlow(ParallelManager par, double[][][] left_right_flow, double[][][] up_down_flow, double[][][] consVals)
    {
        calculateBorderFlow(par, left_right_flow, up_down_flow, consVals);
        Restorator2dUtility restorator = new Restorator2dUtility(rawRestorator, consVals, gamma);
        int xRes = consVals.length;
        int yRes = consVals[0].length;
        double[] uLeft_phy = new double[8];
        double[] uRight_phy = new double[8];
        double[] uUp_phy = new double[8];
        double[] uDown_phy = new double[8];

        for (int i : par.range(1, xRes - 2, true))
        {
            for (int j = 1; j < yRes - 1; j++)
            {
                double[] flow = left_right_flow[i][j];
                restorator.restoreLeft(uLeft_phy, i, j);
                restorator.restoreRight(uRight_phy, i, j);
                setFlow(flow, uLeft_phy, uRight_phy, i, j, 1.0, 0.0);
            }
        }
        for (int i : par.range(1, xRes - 1, false))
        {
            for (int j = 1; j < yRes - 2; j++)
            {
                double[] flow = up_down_flow[i][j];
                restorator.restoreUp(uUp_phy, i, j);
                restorator.restoreDown(uDown_phy, i, j);
                setFlow(flow, uDown_phy, uUp_phy, i, j, 0.0, 1.0);
            }
        }
    }

    private void calculateBorderFlow(ParallelManager par, double[][][] left_right_flow, double[][][] up_down_flow, double[][][] consVals)
    {
        int xRes = consVals.length;
        int yRes = consVals[0].length;
        double[] u_temp_0 = new double[8];
        double[] u_temp_1 = new double[8];

        for (int j : par.range(1, yRes - 1, true))
        {
            double[] flow_left = left_right_flow[0][j];
            toPhysical(u_temp_0, consVals[0][j], gamma);
            toPhysical(u_temp_1, consVals[1][j], gamma);
            setFlow(flow_left, u_temp_0, u_temp_1, 0, j, 1.0, 0.0);

            double[] flow_right = left_right_flow[xRes - 2][j];
            toPhysical(u_temp_0, consVals[xRes - 2][j], gamma);
            toPhysical(u_temp_1, consVals[xRes - 1][j], gamma);
            setFlow(flow_right, u_temp_0, u_temp_1, xRes - 2, j, 1.0, 0.0);
        }
        for (int i : par.range(1, xRes - 1, false))
        {
            toPhysical(u_temp_0, consVals[i][0], gamma);
            toPhysical(u_temp_1, consVals[i][1], gamma);
            double[] flow_down = up_down_flow[i][0];
            setFlow(flow_down, u_temp_0, u_temp_1, i, 0, 0.0, 1.0);

            toPhysical(u_temp_0, consVals[i][yRes - 2], gamma);
            toPhysical(u_temp_1, consVals[i][yRes - 1], gamma);
            double[] flow_up = up_down_flow[i][yRes - 2];
            setFlow(flow_up, u_temp_0, u_temp_1, i, yRes - 2, 0.0, 1.0);
        }
    }
}
