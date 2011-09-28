package ru.vasily.solver.restorator;

public class NoOpRestorator implements ThreePointRestorator
{

	@Override
	public double restore(double vLeft, double vRight, double vRightRight)
	{
		return vRight;
	}

}
