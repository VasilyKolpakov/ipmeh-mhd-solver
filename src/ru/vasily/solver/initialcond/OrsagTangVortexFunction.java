package ru.vasily.solver.initialcond;

import ru.vasily.solver.MHDValues;
import ru.vasily.solver.Utils;
import static java.lang.Math.*;

public class OrsagTangVortexFunction implements Init2dFunction
{

	private final double gamma;

	public OrsagTangVortexFunction(double gamma)
	{
		this.gamma = gamma;
	}

	@Override
	public void apply(double[] arr, double x, double y)
	{
		double b_0 = 1 / sqrt(4 * PI);
		MHDValues val = MHDValues.builder().
				rho(25 / (36 * PI)).
				p(5 / (12 * PI)).
				u(-sin(2 * PI * y)).
				v(sin(2 * PI * x)).
				w(0).
				bX(-b_0 * sin(2 * PI * y)).
				bY(b_0 * sin(4 * PI * x)).
				bZ(0).
				build();
		Utils.setCoservativeValues(val, arr, gamma);
	}
}
