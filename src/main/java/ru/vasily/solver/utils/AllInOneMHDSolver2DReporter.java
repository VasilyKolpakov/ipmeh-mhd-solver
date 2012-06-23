package ru.vasily.solver.utils;

import ru.vasily.application.plotdata.PlotData;
import ru.vasily.solver.restorator.MinmodRestorator;

import static java.lang.Math.*;
import static ru.vasily.core.collection.Range.range;
import static ru.vasily.solver.Utils.toPhysical;
import static ru.vasily.solver.Utils.valueNumber;
import static ru.vasily.application.plotdata.PlotDataFactory.*;

public class AllInOneMHDSolver2DReporter implements MHDSolver2DReporter
{

    @Override
    public PlotData report(double[][] x, double[][] y, double[][][] val,
                           double[][][] up_down_flow, double[][][] left_right_flow, double gamma)
    {
        return new ReportObj(x, y, val, up_down_flow, left_right_flow, gamma).report();
    }

    private class ReportObj
    {

        private final double[][] x;
        private final double[][] y;
        private final double[][][] val;
        private final double[][][] up_down_flow;
        private final double[][][] left_right_flow;
        private final double gamma;
        private final int xRes;
        private final int yRes;

        public ReportObj(double[][] x, double[][] y, double[][][] val,
                         double[][][] up_down_flow, double[][][] left_right_flow, double gamma)
        {
            this.x = x;
            this.y = y;
            this.val = val;
            this.up_down_flow = up_down_flow;
            this.left_right_flow = left_right_flow;
            this.gamma = gamma;
            xRes = val.length;
            yRes = val[0].length;
        }

        public PlotData report()
        {
            double[][][] phy = getPhysical(val);
            return plots(
                    plot1D("density", getXCoord(), getXSlice(phy, 0)),
                    plot1D("density_y", getYCoord(), getYSlice(phy, 0)),
                    plot1D("u", getXCoord(), getXSlice(phy, 1)),
                    plot1D("v", getXCoord(), getXSlice(phy, 2)),
                    plot1D("w", getXCoord(), getXSlice(phy, 3)),
                    plot1D("thermal_pressure", getXCoord(), getXSlice(phy, 4)),
                    plot1D("bX", getXCoord(), getXSlice(phy, 5)),
                    plot1D("bY", getXCoord(), getXSlice(phy, 6)),
                    plot1D("bZ", getXCoord(), getXSlice(phy, 7)),
                    plot2D("density_2d", x, y, physicalValue("rho")),
                    plot2D("u_2d", x, y, physicalValue("u")),
                    plot2D("v_2d", x, y, physicalValue("v")),
                    plot2D("w_2d", x, y, physicalValue("w")),
                    plot2D("pressure_2d", x, y, physicalValue("p")),
                    plot2D("schlieren_2d", x, y, schlieren()),
                    plot2D("magnetic_pressure_2d", x, y, magneticPressure()),
                    plot2D("bX_2d", x, y, physicalValue("bX")),
                    plot2D("abs_speed_2d", x, y, speed()),
                    plot2D("full_energy_2d", x, y, fullEnergy()),
                    plot2D("divB_2d", x, y, divB(val)),
                    plot2D("dRho_dt_2d", x, y, dRho_dt()));
        }

        private double[][] divB(double[][][] consVals)
        {
            Restorator2dUtility restorator = new Restorator2dUtility(new MinmodRestorator(), consVals,
                                                                     gamma);
            // TODO hack!!
            double hy = y[1][1] - y[1][0];
            double hx = x[1][1] - x[0][1];
            double[][] divB = newArray2d();
            double[] temp = new double[8];
            for (int i : range(2, xRes - 1))
            {
                for (int j : range(2, yRes - 1))
                {
                    double bY_cell_top = restorator.restoreDown(temp, i, j)[6];
                    double bY_cell_bottom = restorator.restoreUp(temp, i, j - 1)[6];
                    double bX_cell_left_side =
                            restorator.restoreRight(temp, i - 1, j)[5];
                    double bX_cell_right_side =
                            restorator.restoreLeft(temp, i, j)[5];
                    divB[i][j] = (bY_cell_top - bY_cell_bottom) / hy +
                            (bX_cell_right_side - bX_cell_left_side) / hx;
                }
            }
            return divB;
        }

        private double[][] dRho_dt()
        {
            // TODO hack!!
            double hy = y[1][1] - y[1][0];
            double hx = x[1][1] - x[0][1];
            double[][] divRho = newArray2d();
            for (int i = 2; i < xRes - 2; i++)
            {
                for (int j = 2; j < yRes - 2; j++)
                {
                    final double up_down_diff = up_down_flow[i][j - 1][0]
                            - up_down_flow[i][j][0];
                    double divRhoVal = up_down_diff / hy;

                    final double left_right_diff = left_right_flow[i - 1][j][0]
                            - left_right_flow[i][j][0];
                    divRhoVal += left_right_diff / hx;
                    divRho[i][j] = divRhoVal;
                }
            }
            return divRho;
        }

        private double[][] fullEnergy()
        {
            double[][] energy = newArray2d();
            for (int i = 1; i < xRes - 1; i++)
            {
                for (int j = 1; j < yRes - 1; j++)
                {
                    double[] conservativeValues = val[i][j];
                    energy[i][j] = conservativeValues[4];
                }
            }
            return energy;
        }

        private double[][] speed()
        {
            double[][] speed = newArray2d();
            double[][] u = physicalValue("u");
            double[][] v = physicalValue("v");
            double[][] w = physicalValue("w");
            for (int i = 1; i < xRes - 1; i++)
            {
                for (int j = 1; j < yRes - 1; j++)
                {
                    speed[i][j] =
                            sqrt(v[i][j] * v[i][j] + u[i][j] * u[i][j] + w[i][j] * w[i][j]);
                }
            }
            return speed;
        }

        private double[][] magneticPressure()
        {
            double[][] magneticPressure = newArray2d();
            double[][] bx = physicalValue("bx");
            double[][] by = physicalValue("by");
            double[][] bz = physicalValue("bz");
            for (int i = 1; i < xRes - 1; i++)
            {
                for (int j = 1; j < yRes - 1; j++)
                {
                    magneticPressure[i][j] = (bx[i][j] * bx[i][j] + by[i][j] * by[i][j] + bz[i][j]
                            * bz[i][j])
                            / 8 / PI;
                }
            }
            return magneticPressure;
        }

        private double[][] schlieren()
        {
            double[][] density = physicalValue("rho");
            double[][] schlieren = newArray2d();
            for (int i = 1; i < xRes - 1; i++)
            {
                for (int j = 1; j < yRes - 1; j++)
                {
                    double gradX = density[i + 1][j] - density[i - 1][j];
                    double gradY = density[i][j + 1] - density[i][j - 1];
                    schlieren[i][j] = sqrt(gradX * gradX + gradY * gradY);
                }
            }
            return schlieren;
        }

        private double[][] newArray2d()
        {
            return new double[xRes][yRes];
        }

        private double[] getXCoord()
        {
            double[] ret = new double[xRes];
            for (int i = 0; i < ret.length; i++)
            {
                ret[i] = x[i][yRes / 2];
            }
            return ret;
        }

        private double[] getYCoord()
        {
            double[] ret = new double[yRes];
            for (int j = 0; j < ret.length; j++)
            {
                ret[j] = y[xRes / 2][j];
            }
            return ret;
        }

        private double[][] physicalValue(String valueName)
        {
            double[][] value = newArray2d();
            double[] temp = new double[8];
            for (int i = 0; i < xRes; i++)
            {
                for (int j = 0; j < yRes; j++)
                {
                    value[i][j] = toPhysical(temp, val[i][j], gamma)[valueNumber(valueName)];
                }
            }
            return value;
        }

        private double[][][] getPhysical(double[][][] consVal)
        {
            int xRes = consVal.length;
            int yRes = consVal[0].length;
            double[][][] ret = new double[xRes][yRes][consVal[0][0].length];
            for (int i = 0; i < xRes; i++)
            {
                for (int j = 0; j < yRes; j++)
                {
                    toPhysical(ret[i][j], consVal[i][j], gamma);
                }
            }
            return ret;
        }

        private double[] getXSlice(double[][][] physical, int valNum)
        {
            double[] ret = new double[physical.length];
            int yCenter = physical[0].length / 2;
            for (int i = 0; i < ret.length; i++)
            {
                ret[i] = physical[i][yCenter][valNum];
            }
            return ret;
        }

        private double[] getYSlice(double[][][] physical, int valNum)
        {
            double[] ret = new double[physical[0].length];
            int xCenter = physical.length / 2;
            for (int i = 0; i < ret.length; i++)
            {
                ret[i] = physical[xCenter][i][valNum];
            }
            return ret;
        }
    }
}
