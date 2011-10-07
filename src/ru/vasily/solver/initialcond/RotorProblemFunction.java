package ru.vasily.solver.initialcond;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDValues;
import ru.vasily.solver.Utils;

import static java.lang.Math.*;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/7/11
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class RotorProblemFunction implements Init2dFunction
{
	private final double u_0;
	private final double p_0;
	private final double rho_0;
	private final double b_0;
	private final double x_c;
	private final double r_0;
	private final double r_1;
	private final double y_c;
	private final double gamma;
	private final double smallLength;

	public RotorProblemFunction(DataObject param, double gamma)
	{
		this.gamma = gamma;
		u_0 = param.getDouble("u_0");
		p_0 = param.getDouble("p_0");
		rho_0 = param.getDouble("rho_0");
		b_0 = param.getDouble("b_0");
		x_c = param.getDouble("x_c");
		y_c = param.getDouble("y_c");
		r_0 = param.getDouble("r_0");
		r_1 = param.getDouble("r_1");
		smallLength = (abs(x_c) + abs(y_c)) * 0.00000000001;
	}

	@Override
	public void apply(double[] arr, double x, double y)
	{
		double dx = x - x_c;
		double dy = y - y_c;
		double r = sqrt(dx * dx + dy * dy) + smallLength; // to avoid division by zero
		MHDValues val = MHDValues.builder().
				p(p_0).
				rho(1 + (rho_0 - 1) * fun(r)).
				u(-u_0 * fun(r) * (y - y_c) / r).
				v(u_0 * fun(r) * (x - x_c) / r).
				w(0).
				bX(b_0).
				bY(0).
				bZ(0).
				build();
		Utils.setCoservativeValues(val, arr, gamma);
	}

	private double fun(double r)
	{
		return max(0, min(1, (r_1 - r) / (r_1 - r_0)));
	}
}
