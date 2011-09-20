package ru.vasily.solver;

import static java.lang.Math.*;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;

public final class Utils
{
	private Utils()
	{
	}

	public static final int iBX = 5;
	public static final int iBY = 6;
	public static final int iBZ = 7;

	public static double getPressure(double[] u, double gamma)
	{
		double ro = u[0];
		double roU = u[1];
		double roV = u[2];
		double roW = u[3];
		double e = u[4];
		double bX = u[5];
		double bY = u[6];
		double bZ = u[7];
		double p = (e
				- (roU * roU + roV * roV + roW * roW) / ro
				/ 2 - (bX * bX + bY * bY + bZ * bZ) / 8
				/ Math.PI)
				* (gamma - 1);
		return p;
	}

	public static double[] toPhysical(double[] result, double[] u, double gamma)
	{
		double ro = u[0];
		double roU = u[1];
		double roV = u[2];
		double roW = u[3];

		double bX = u[5];
		double bY = u[6];
		double bZ = u[7];
		double Rho = ro;

		double U = roU / ro;
		double V = roV / ro;
		double W = roW / ro;
		double PGas = getPressure(u, gamma);
		double BX = bX;
		double BY = bY;
		double BZ = bZ;
		result[0] = Rho;
		result[1] = U;
		result[2] = V;
		result[3] = W;
		result[4] = PGas;
		result[5] = BX;
		result[6] = BY;
		result[7] = BZ;
		return result;
	}

	/**
	 * Kulikovskij_MatematVoprosiChislenResheniyaGiperbol.djvu page 338
	 * 
	 * @param u_phy
	 * @param bN
	 *            normal field component
	 * @param gamma
	 * @return
	 */
	public static double fastShockSpeed(double[] u_phy, double bN, double gamma)
	{
		double ro = u_phy[0];
		// double U = u_phy[1];
		// double V = u_phy[2];
		// double W = u_phy[3];
		double PGas = u_phy[4];
		double bX = u_phy[5];
		double bY = u_phy[6];
		double bZ = u_phy[7];

		double b_square_div4piRo = (bX * bX + bY * bY + bZ
				* bZ)
				/ (4 * PI * ro);
		double speedOfSound_square = gamma * PGas / ro;
		double speedOfSound = sqrt(speedOfSound_square);
		double absBx = abs(bN);
		double third = absBx * speedOfSound / sqrt(PI * ro);
		double cf = 0.5 *
				(
				sqrt(speedOfSound_square + b_square_div4piRo + third) +
				sqrt(speedOfSound_square + b_square_div4piRo - third)
				);
		return cf;
	}


	private static Map<String, Integer> valueNumbers = ImmutableMap.<String, Integer> builder()
			.put("rho", 0)
			.put("u", 1)
			.put("v", 2)
			.put("w", 3)
			.put("p", 4)
			.put("bx", 5)
			.put("by", 6)
			.put("bz", 7)
			.build();

	public static int valueNumber(String valueName)
	{
		Integer integer = valueNumbers.get(valueName.toLowerCase());
		Preconditions.checkNotNull(integer, "illegal value name \'%s\'", valueName);
		return integer;
	}
}
