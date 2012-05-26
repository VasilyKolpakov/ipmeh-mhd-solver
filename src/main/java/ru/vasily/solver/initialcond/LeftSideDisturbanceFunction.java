package ru.vasily.solver.initialcond;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static ru.vasily.core.dataobjs.DataObjects.getDouble;

public class LeftSideDisturbanceFunction implements Init2dFunction
{

    private final double k_x;
    private final double k_y;
    private final double rhoAmp;
    private final double uAmp;
    private final double x_c;
    private final double xLength;
    private final double yLength;
    private final double y_0;
    private final double x_0;
    private final MHDValues left;
    private final double gamma;
    private final double sin_ψ;
    private final double cos_ψ;

    public LeftSideDisturbanceFunction(DataObject conditionsData, DataObject physicalConstants, MHDValues left)
    {
        this.left = left;
        x_c = conditionsData.getDouble("x_s");
        k_x = conditionsData.getDouble("k_x");
        k_y = conditionsData.getDouble("k_y");
        rhoAmp = conditionsData.getDouble("rhoAmpRel") * left.rho;
        uAmp = conditionsData.getDouble("uAmpRel") * sqrt(left.u * left.u + left.v * left.v);
        this.gamma = physicalConstants.getDouble("gamma");
        this.xLength = physicalConstants.getDouble("xLength");
        this.yLength = physicalConstants.getDouble("yLength");
        y_0 = getDouble(physicalConstants, "y_0", 0.0);
        x_0 = getDouble(physicalConstants, "x_0", 0.0);
        sin_ψ = k_y / sqrt(k_x * k_x + k_y * k_y);
        cos_ψ = k_x / sqrt(k_x * k_x + k_y * k_y);
    }

    @Override
    public void apply(double[] arr, double x_, double y_)
    {
        if (x_ < x_c)
        {
            double y_rel = (y_ - y_0) / yLength;
            double x_rel = (x_ - x_0) / xLength;
            double rho_d_0 = rhoAmp * cos((k_y * y_rel + k_x * x_rel) * 2 * PI);
            double u_d = uAmp * sin_ψ * cos((k_y * y_rel + k_x * x_rel) * 2 * PI);
            double v_d = -uAmp * cos_ψ * cos((k_y * y_rel + k_x * x_rel) * 2 * PI);
            MHDValues val_0 = MHDValues.builder()
                                       .rho(left.rho + rho_d_0)
                                       .p(left.p)
                                       .u(left.u + u_d)
                                       .v(left.v + v_d)
                                       .w(left.w)
                                       .bX(left.bX)
                                       .bY(left.bY)
                                       .bZ(left.bZ)
                                       .build();
            val_0.setToArray(arr, gamma);
        }
    }
}
