package ru.vasily.solver.riemann;

public class RiemannSolver1Dto2DWrapper implements RiemannSolver2D
{

    private final RiemannSolver solver;

    public RiemannSolver1Dto2DWrapper(RiemannSolver solver)
    {
        this.solver = solver;
    }

    @Override
    public void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR, double cos_alfa, double sin_alfa)
    {
        double RhoL = uL[0];
        double UL = uL[1];
        double VL = uL[2];
        double WL = uL[3];
        double PGasL = uL[4];
        double BXL = uL[5];
        double BYL = uL[6];
        double BZL = uL[7];

        double RhoR = uR[0];
        double UR = uR[1];
        double VR = uR[2];
        double WR = uR[3];
        double PGasR = uR[4];
        double BXR = uR[5];
        double BYR = uR[6];
        double BZR = uR[7];

        double UL_rotated = rotate_x(cos_alfa, -sin_alfa, UL, VL);
        double VL_rotated = rotate_y(cos_alfa, -sin_alfa, UL, VL);

        double UR_rotated = rotate_x(cos_alfa, -sin_alfa, UR, VR);
        double VR_rotated = rotate_y(cos_alfa, -sin_alfa, UR, VR);

        double BXL_rotated = rotate_x(cos_alfa, -sin_alfa, BXL, BYL);
        double BYL_rotated = rotate_y(cos_alfa, -sin_alfa, BXL, BYL);

        double BXR_rotated = rotate_x(cos_alfa, -sin_alfa, BXR, BYR);
        double BYR_rotated = rotate_y(cos_alfa, -sin_alfa, BXR, BYR);

        solver.getFlow(flow, RhoL, UL_rotated, VL_rotated, WL, PGasL, BXL_rotated, BYL_rotated,
                       BZL, gammaL, RhoR, UR_rotated, VR_rotated, WR, PGasR, BXR_rotated, BYR_rotated,
                       BZR, gammaR);

        rotate(flow, cos_alfa, sin_alfa);
    }

    private double rotate_x(double alfa_re, double alfa_im, double Ux, double Uy)
    {
        return Ux * alfa_re - Uy * alfa_im;
    }

    private double rotate_y(double alfa_re, double alfa_im, double Ux, double Uy)
    {
        return Ux * alfa_im + Uy * alfa_re;
    }

    private void rotate(double[] flow, double alfa_re, double alfa_im)
    {
        double roU = flow[1];
        double roV = flow[2];
        double bX = flow[5];
        double bY = flow[6];
        double roU_rotated = rotate_x(alfa_re, alfa_im, roU, roV);
        double roV_rotated = rotate_y(alfa_re, alfa_im, roU, roV);

        double bX_rotated = rotate_x(alfa_re, alfa_im, bX, bY);
        double bY_rotated = rotate_y(alfa_re, alfa_im, bX, bY);

        flow[1] = roU_rotated;
        flow[2] = roV_rotated;
        flow[5] = bX_rotated;
        flow[6] = bY_rotated;
    }

}
