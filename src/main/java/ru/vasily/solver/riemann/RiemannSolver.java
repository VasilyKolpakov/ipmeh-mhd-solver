package ru.vasily.solver.riemann;

public interface RiemannSolver
{
    void getFlow(double[] flow, double RhoL, double UL, double VL,
                 double WL, double PGasL, double BXL, double BYL, double BZL,
                 double GamL, double RhoR, double UR, double VR, double WR,
                 double PGasR, double BXR, double BYR, double BZR, double GamR);

    void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR);
}
