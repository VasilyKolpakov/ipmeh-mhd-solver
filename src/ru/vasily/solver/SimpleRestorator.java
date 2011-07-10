package ru.vasily.solver;

import static java.lang.Math.*;

public class SimpleRestorator implements TreePointRestorator
{
	@Override
	public double restore(double vLeft, double vRight, double vRightRight)
	{
		return vRight - minmod(vRight - vLeft, vRight - vRightRight);
	}

	private double minmod(double d1, double d2)
	{
		return (signum(d1) + signum(d2)) * (min(abs(d1), abs(d2))) * 0.5;
	}
}
