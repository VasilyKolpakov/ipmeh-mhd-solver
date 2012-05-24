package ru.vasily.solver.border;

import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class LeftDisturbanceWave implements Array2dBorderConditions
{

    private final int yRes;
    private final int xRes;
    private final MHDValues leftAverage;
    private final double gamma;
    private final double k_x;
    private final double k_y;
    private final double rhoAmp;
    private final double uAmp;
    private final double xLength;
    private final double yLength;
    private final double k_t;

    public LeftDisturbanceWave(DataObject calculationConstants,
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
        this.k_t = k_x * leftAverage.u + k_y * leftAverage.v;
        this.rhoAmp = conditionsData.getDouble("rhoAmpRel") * leftAverage.rho;
        this.uAmp = conditionsData.getDouble("uAmpRel") * leftAverage.u;
        this.xLength = physicalConstants.getDouble("xLength");
        this.yLength = physicalConstants.getDouble("yLength");
        System.out.println("LeftDisturbanceWave.LeftDisturbanceWave");
        System.out.printf("rhoAmp = %s, uAmp = %s", rhoAmp, uAmp);
    }

    @Override
    public void applyConditions(double[][][] values, double time)
    {
        for (int i = 0; i < yRes; i++)
        {
            double y = (double) i / (double) yRes;
            double rho_d_0 = rhoAmp * cos((k_y * y - k_t * time) * 2 * PI);
            double u_d_0 = uAmp * cos((k_y * y - k_t * time) * 2 * PI);
            MHDValues val_0 = MHDValues.builder()
                                       .rho(leftAverage.rho + rho_d_0)
                                       .p(leftAverage.p)
                                       .u(leftAverage.u)//+ u_d_0)
                                       .v(leftAverage.v)
                                       .w(leftAverage.w)
                                       .bX(leftAverage.bX)
                                       .bY(leftAverage.bY)
                                       .bZ(leftAverage.bZ)
                                       .build();
            val_0.setToArray(values[0][i], gamma);

//            double rho_d_1 = rhoAmp * cos(k_x * d_x + k_y * d_y - k_t * time);
//            double u_d_1 = uAmp * cos(k_x * d_x + k_y * d_y - k_t * time);
//            MHDValues val_1 = MHDValues.builder()
//                                       .rho(leftAverage.rho + rho_d_1)
//                                       .p(leftAverage.p)
//                                       .u(leftAverage.u + u_d_1)
//                                       .v(leftAverage.v)
//                                       .w(leftAverage.w)
//                                       .bX(leftAverage.bX)
//                                       .bY(leftAverage.bY)
//                                       .bZ(leftAverage.bZ)
//                                       .build();
//            val_1.setToArray(values[1][i], gamma);
        }
    }
}