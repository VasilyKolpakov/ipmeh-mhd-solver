package ru.vasily.solver;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import ru.vasily.dataobjs.DataObject;

import java.util.Map;

import static java.lang.Math.*;

public final class Utils
{
    private Utils()
    {
    }

    public static final int iBX = 5;
    public static final int iBY = 6;
    public static final int iBZ = 7;

    public static double getPressure(double[] u, double gamma)
    {
        double ro = u[0];
        double roU = u[1];
        double roV = u[2];
        double roW = u[3];
        double e = u[4];
        double bX = u[5];
        double bY = u[6];
        double bZ = u[7];
        double p = (e
                - (roU * roU + roV * roV + roW * roW) / ro
                / 2 - (bX * bX + bY * bY + bZ * bZ) / 8
                / Math.PI)
                * (gamma - 1);
        return p;
    }

    public static double[] toPhysical(double[] result, double[] u, double gamma)
    {
        double ro = u[0];
        double roU = u[1];
        double roV = u[2];
        double roW = u[3];

        double bX = u[5];
        double bY = u[6];
        double bZ = u[7];
        double Rho = ro;

        double U = roU / ro;
        double V = roV / ro;
        double W = roW / ro;
        double PGas = getPressure(u, gamma);
        double BX = bX;
        double BY = bY;
        double BZ = bZ;
        result[0] = Rho;
        result[1] = U;
        result[2] = V;
        result[3] = W;
        result[4] = PGas;
        result[5] = BX;
        result[6] = BY;
        result[7] = BZ;
        return result;
    }

    /**
     * Kulikovskij_MatematVoprosiChislenResheniyaGiperbol.djvu page 338
     *
     * @param u_phy
     * @param bN    normal field component
     * @param gamma
     * @return
     */
    public static double fastShockSpeed(double[] u_phy, double bN, double gamma)
    {
        double ro = u_phy[0];
        // double U = u_phy[1];
        // double V = u_phy[2];
        // double W = u_phy[3];
        double PGas = u_phy[4];
        double bX = u_phy[5];
        double bY = u_phy[6];
        double bZ = u_phy[7];

        double b_square_div4piRo = (bX * bX + bY * bY + bZ * bZ)
                / (4 * PI * ro);
        double speedOfSound_square = gamma * PGas / ro;
        double speedOfSound = sqrt(speedOfSound_square);
        double absBx = abs(bN);
        double third = absBx * speedOfSound / sqrt(PI * ro);
        double cf = 0.5 *
                (
                        sqrt(speedOfSound_square + b_square_div4piRo + third) +
                                sqrt(speedOfSound_square + b_square_div4piRo - third)
                );
        return cf;
    }

    public static double maximumFastShockSpeed(double[] u_phy, double gamma)
    {
        double ro = u_phy[0];
        // double U = u_phy[1];
        // double V = u_phy[2];
        // double W = u_phy[3];
        double PGas = u_phy[4];
        double bX = u_phy[5];
        double bY = u_phy[6];
        double bZ = u_phy[7];
        double b_square_div4piRo = (bX * bX + bY * bY + bZ * bZ)
                / (4 * PI * ro);
        double speedOfSound_square = gamma * PGas / ro;
        return sqrt(speedOfSound_square + b_square_div4piRo);
    }

    private static Map<String, Integer> valueNumbers = ImmutableMap.<String, Integer>builder()
            .put("rho", 0)
            .put("u", 1)
            .put("v", 2)
            .put("w", 3)
            .put("p", 4)
            .put("bx", 5)
            .put("by", 6)
            .put("bz", 7)
            .build();

    public static int valueNumber(String valueName)
    {
        Integer integer = valueNumbers.get(valueName.toLowerCase());
        Preconditions.checkNotNull(integer, "illegal value name \'%s\'", valueName);
        return integer;
    }

    public static void setCoservativeValues(DataObject data, double[] u, double gamma)
    {
        double rhoL = data.getDouble("rho");
        double pL = data.getDouble("p");
        double uL = data.getDouble("u");
        double vL = data.getDouble("v");
        double wL = data.getDouble("w");
        double bXL = data.getDouble("bX");
        double bYL = data.getDouble("bY");
        double bZL = data.getDouble("bZ");
        u[0] = rhoL;
        u[1] = rhoL * uL;
        u[2] = rhoL * vL;
        u[3] = rhoL * wL;
        u[4] = pL / (gamma - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
                + (bYL * bYL + bZL * bZL + bXL * bXL) / 8 / PI;
        u[5] = bXL;
        u[6] = bYL;
        u[7] = bZL;
    }

    public static void setCoservativeValues(MHDValues values, double[] u_val, double gamma)
    {
        double rho = values.rho;
        double p = values.p;
        double u = values.u;
        double v = values.v;
        double w = values.w;
        double bX = values.bX;
        double bY = values.bY;
        double bZ = values.bZ;
        u_val[0] = rho;
        u_val[1] = rho * u;
        u_val[2] = rho * v;
        u_val[3] = rho * w;
        u_val[4] = p / (gamma - 1) + rho * (u * u + v * v + w * w) / 2
                + (bY * bY + bZ * bZ + bX * bX) / 8 / PI;
        u_val[5] = bX;
        u_val[6] = bY;
        u_val[7] = bZ;
    }

}
