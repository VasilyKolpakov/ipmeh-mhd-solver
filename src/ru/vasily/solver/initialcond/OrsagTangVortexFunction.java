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
		double beta_0 = 2 * gamma;
		double p_0 = beta_0 / (8 * PI);
		double M_0 = 1;
		MHDValues val = MHDValues.builder().
				rho(gamma * p_0 * M_0).
				p(p_0).
				u(-sin(2 * PI * y)).
				v(sin(2 * PI * x)).
				w(0).
				bX(-sin(2 * PI * y)).
				bY(sin(4 * PI * x)).
				bZ(0).
				build();
		Utils.setCoservativeValues(val, arr, gamma);
	}
}
