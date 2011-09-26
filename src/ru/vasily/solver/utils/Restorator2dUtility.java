package ru.vasily.solver.utils;

import ru.vasily.solver.restorator.ThreePointRestorator;
import static ru.vasily.solver.Utils.*;

public class Restorator2dUtility
{
	private final ThreePointRestorator restorator;
	private final double[][][] consVal;
	private final double[] temp_1 = new double[8];
	private final double[] temp_2 = new double[8];
	private final double[] temp_3 = new double[8];
	private final double gamma;

	public Restorator2dUtility(ThreePointRestorator restorator,
			double[][][] consVal, double gamma)
	{
		this.restorator = restorator;
		this.consVal = consVal;
		this.gamma = gamma;
	}

	public void restoreUp(double[] up_phi, int i, int j)
	{
		restore(up_phi,
				i, j,
				i, j + 1,
				i, j + 2);
	}

	public void restoreDown(double[] up_phi, int i, int j)
	{
		restore(up_phi,
				i, j + 1,
				i, j,
				i, j - 1);
	}

	public void restoreLeft(double[] up_phi, int i, int j)
	{
		restore(up_phi,
				i + 1, j,
				i, j,
				i - 1, j);
	}

	public void restoreRight(double[] up_phi, int i, int j)
	{
		restore(up_phi,
				i, j,
				i + 1, j,
				i + 2, j);
	}

	private void restore(double[] u_phi, int i1, int j1, int i2, int j2,
			int i3, int j3)
	{
		double[] u_1 = _toPhysical(temp_1, i1, j1);
		double[] u_2 = _toPhysical(temp_2, i2, j2);
		double[] u_3 = _toPhysical(temp_3, i3, j3);
		restore(u_phi, u_1, u_2, u_3);
	}

	private double[] _toPhysical(double[] u_phy, int i, int j)
	{
		return toPhysical(u_phy, consVal[i][j], gamma);
	}

	private void restore(double[] u_phy, double[] u_1, double[] u_2,
			double[] u_3)
	{
		for (int k = 0; k < 8; k++)
		{
			u_phy[k] = restorator.restore(u_1[k], u_2[k], u_3[k]);
		}
	}

}
