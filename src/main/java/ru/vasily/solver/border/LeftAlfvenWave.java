package ru.vasily.solver.border;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.Utils;

import static java.lang.Math.*;

public class LeftAlfvenWave implements Array2dBorderConditions
{

    private final int yRes;
    private final int xRes;
    private final MHDValues leftAverage;
    private final double gamma;
    private final double k_x;
    private final double k_y;
    private final double amp;
    private final double xLength;
    private final double yLength;
    private final double k_t;
    private final double cos_ψ;
    private final double sin_ψ;

    public LeftAlfvenWave(DataObject calculationConstants,
                          DataObject physicalConstants,
                          MHDValues leftAverage,
                          DataObject conditionsData)
    {
        this.yRes = calculationConstants.getInt("yRes");
        this.xRes = calculationConstants.getInt("xRes");
        this.leftAverage = leftAverage;
        this.gamma = physicalConstants.getDouble("gamma");

        this.k_x = conditionsData.getDouble("k_x");
        this.k_y = conditionsData.getDouble("k_y");
        this.k_t = (leftAverage.bX * k_x + leftAverage.bY * k_y) / sqrt(leftAverage.rho)
                - k_x * leftAverage.u - k_y * leftAverage.v;
        sin_ψ = k_y / sqrt(k_x * k_x + k_y * k_y);
        cos_ψ = k_x / sqrt(k_x * k_x + k_y * k_y);
        this.amp = conditionsData.getDouble("amp")
                * sqrt(leftAverage.u * leftAverage.u + leftAverage.v * leftAverage.v);
        this.xLength = physicalConstants.getDouble("xLength");
        this.yLength = physicalConstants.getDouble("yLength");
        System.out.println("LeftDisturbanceWave.LeftDisturbanceWave");
        System.out.printf("amp = %s\n", amp);
    }

    @Override
    public void applyConditions(double[][][] values, double time)
    {
        double[] phyVals = new double[8];
        final double rho_sqrt = sqrt(leftAverage.rho);
        for (int i = 0; i < yRes; i++)
        {
            double y = (double) i / (double) (yRes - 1);
            final double phase = (k_y * y + k_t * time) * 2 * PI;
            final double phase_cos = cos(phase);
            double u_d = -amp * sin_ψ * phase_cos;
            double v_d = amp * cos_ψ * phase_cos;
            final double phase_sin = sin(phase);
            double w_d = amp * phase_sin;

            double bX_d = -amp * rho_sqrt * sin_ψ * phase_cos;
            double bY_d = amp * rho_sqrt * cos_ψ * phase_cos;
            double bZ = amp * rho_sqrt * phase_sin;

            phyVals[0] = leftAverage.rho;
            phyVals[1] = leftAverage.u + u_d;
            phyVals[2] = leftAverage.v + v_d;
            phyVals[3] = leftAverage.w + w_d;
            phyVals[4] = leftAverage.p;
            phyVals[5] = leftAverage.bX + bX_d;
            phyVals[6] = leftAverage.bY + bY_d;
            phyVals[7] = bZ;
            Utils.setConservativeValues(phyVals, values[0][i], gamma);
        }
    }
}
