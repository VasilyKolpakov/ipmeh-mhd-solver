package ru.vasily.solver.utils;

import ru.vasily.solverhelper.PlotData;

import static java.lang.Math.sqrt;
import static ru.vasily.solver.Utils.toPhysical;
import static ru.vasily.solver.Utils.valueNumber;
import static ru.vasily.solverhelper.PlotDataFactory.*;

public class AllInOneMHDSolver2DReporter implements MHDSolver2DReporter
{

	@Override
	public PlotData report(double[][] x, double[][] y, double[][][] val,
						   double[][] divB,
						   double gamma)
	{
		return new ReportObj(x, y, val, divB, gamma).report();
	}

	private class ReportObj
	{

		private final double[][] x;
		private final double[][] y;
		private final double[][][] val;
		private final double gamma;
		private final int xRes;
		private final int yRes;
		private final double[][] divB;

		public ReportObj(double[][] x, double[][] y, double[][][] val,
						 double[][] divB, double gamma)
		{
			this.x = x;
			this.y = y;
			this.val = val;
			this.divB = divB;
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
					plot1D("bY", getXCoord(), getXSlice(phy, 6)),
					plot1D("bZ", getXCoord(), getXSlice(phy, 7)),
					plot2D("density_2d", x, y, physicalValue("rho")),
					plot2D("pressure_2d", x, y, physicalValue("p")),
					plot2D("schlieren_2d", x, y, schlieren()),
					plot2D("divB_2d", x, y, divB));
		}

		private double[][] schlieren()
		{
			double[][] density = physicalValue("rho");
			double[][] schlieren = new double[xRes][yRes];
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
			double[][] value = new double[xRes][yRes];
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
				for (int j = 0; j < yRes; j++)
				{
					toPhysical(ret[i][j], consVal[i][j], gamma);
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
