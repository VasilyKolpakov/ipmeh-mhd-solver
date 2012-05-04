package ru.vasily.solver.restorator;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.signum;

public class EtaOmegaRestorator implements ThreePointRestorator
{
    private final double eta;
    private final double omega;

    public EtaOmegaRestorator(double eta, double omega)
    {
        this.eta = eta;
        this.omega = omega;
    }

    @Override
    public double restore(double vLeft, double vRight, double vRightRight)
    {
        double firstDiffOmegaMinmod = minmod(omega * (vRight - vLeft), vRightRight - vRight);
        double secondDiffOmegaMinmod = minmod(vRight - vLeft, omega * (vRightRight - vRight));
        return vRight - 0.25 * (
                (1.0 + eta) * firstDiffOmegaMinmod
                        + (1.0 - eta) * secondDiffOmegaMinmod
        );
    }

    private double minmod(double d1, double d2)
    {
        final double minmod;
        if (d1 * d2 > 0)
        {
            minmod = signum(d2) * min(abs(d1), abs(d2));
        }
        else
        {
            minmod = 0;
        }
        return minmod;
    }
}
