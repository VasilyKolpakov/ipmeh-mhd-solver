package ru.vasily.solver.initialcond;

import ru.vasily.solver.initialcond.Init2dFunction;

public class MagneticChargeSpotFunc implements Init2dFunction
{

	private final double xSpot;
	private final double ySpot;
	private final double spot_radius_squared;
	private final double coeff;

	public MagneticChargeSpotFunc(double xSpot, double ySpot, double spot_radius, double divB)
	{
		super();
		this.xSpot = xSpot;
		this.ySpot = ySpot;
		this.spot_radius_squared = spot_radius * spot_radius;
		this.coeff = spot_radius_squared * divB / 2;
	}

	@Override
	public void apply(double[] arr, double x, double y)
	{
		double r_x = x - xSpot;
		double r_y = y - ySpot;
		double r_squared = r_x * r_x + r_y * r_y;
		double bx;
		double by;
		if (r_squared > spot_radius_squared)
		{
			bx = r_x / r_squared * coeff;
			by = r_y / r_squared * coeff;
		}
		else
		{
			bx = r_x / spot_radius_squared * coeff;
			by = r_y / spot_radius_squared * coeff;
		}
		arr[5] /* bX */+= bx;
		arr[6] /* bY */+= by;
	}

}
