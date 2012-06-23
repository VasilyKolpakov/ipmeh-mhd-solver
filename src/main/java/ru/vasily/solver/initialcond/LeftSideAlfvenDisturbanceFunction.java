package ru.vasily.solver.initialcond;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.Utils;

import static java.lang.Math.*;
import static ru.vasily.core.dataobjs.DataObjects.getDouble;

public class LeftSideAlfvenDisturbanceFunction implements Init2dFunction
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
    private final double x_s;
    private final double y_0;
    private final double x_0;


    public
    LeftSideAlfvenDisturbanceFunction(DataObject conditionsData,
                                             DataObject physicalConstants,
                                             DataObject calculationConstants,
                                             MHDValues leftAverage)
    {
        this.yRes = calculationConstants.getInt("yRes");
        this.xRes = calculationConstants.getInt("xRes");
        this.leftAverage = leftAverage;
        this.gamma = physicalConstants.getDouble("gamma");
        x_s = conditionsData.getDouble("x_s");

        y_0 = getDouble(physicalConstants, "y_0", 0.0);
        x_0 = getDouble(physicalConstants, "x_0", 0.0);

        this.k_x = conditionsData.getDouble("k_x");
        this.k_y = conditionsData.getDouble("k_y");
        this.k_t = (leftAverage.bX * k_x + leftAverage.bY * k_y) / sqrt(leftAverage.rho)
                - k_x * leftAverage.u + -k_y * leftAverage.v;
        sin_ψ = k_y / sqrt(k_x * k_x + k_y * k_y);
        cos_ψ = k_x / sqrt(k_x * k_x + k_y * k_y);
        this.amp = conditionsData.getDouble("amp")
                * sqrt(leftAverage.u * leftAverage.u + leftAverage.v * leftAverage.v);
        this.xLength = physicalConstants.getDouble("xLength");
        this.yLength = physicalConstants.getDouble("yLength");
    }

    @Override
    public void apply(double[] arr, double x_, double y_)
    {
        double[] phyVals = new double[8];
        final double rho_sqrt = sqrt(leftAverage.rho);
        if (x_ < x_s)
        {
            final double phase = (k_y * y_ + k_x * x_) * 2 * PI;
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
            Utils.setConservativeValues(phyVals, arr, gamma);
        }
    }
}
