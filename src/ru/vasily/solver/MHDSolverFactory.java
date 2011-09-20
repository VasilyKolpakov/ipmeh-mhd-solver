package ru.vasily.solver;

import static java.lang.Math.PI;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver2D.Coordinate;
import ru.vasily.solver.utils.ArrayInitializers;

public class MHDSolverFactory implements IMHDSolverFactory
{

	@Override
	public MHDSolver createSolver(DataObject params)
	{
		String solverType = params.getString("solver");
		if (solverType.equalsIgnoreCase("1d"))
		{
			return solver1d(params);
		}
		else if (solverType.equalsIgnoreCase("2d"))
		{
			return solver2d(params);
		}
		throw new RuntimeException("incorrect solver type : " + solverType);
	}

	private MHDSolver2D solver2d(DataObject params)
	{
		return new MHDSolver2D(params, new RiemannSolver1Dto2DWrapper(new RoeSolverByKryukov()),
				initialValues2d(params));
	}

	private MHDSolver1D solver1d(DataObject params)
	{
		return new MHDSolver1D(params, new RoeSolverByKryukov(), initialValues1d(params));
	}

	private double[][] initialValues1d(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");
		int xRes = calculationConstants.getInt("xRes");
		double gamma = physicalConstants.getDouble("gamma");
		double[][] initVals = new double[xRes][8];
		double xLength = physicalConstants
				.getDouble("xLength");
		int middle = (int) (xRes * (physicalConstants.getDouble("xMiddlePoint") / xLength));
//		for (DataObject initData : params.getObjects("initial_conditions_1d"))
//		{
//			int begin = (int) (xRes*initData.getDouble("begin")/xLength);
//			int begin = (int) (xRes*initData.getDouble("begin")/xLength);
//			for (int i = 0; i < middle; i++)
//			{
//				double[] u = initVals[i];
//				setCoservativeValues(left, u, gamma);
//			}			
//		}
		for (int i = 0; i < middle; i++)
		{
			double[] u = initVals[i];
			setCoservativeValues(left, u, gamma);
		}
		for (int i = middle; i < xRes; i++)
		{
			double[] u = initVals[i];
			setCoservativeValues(right, u, gamma);
		}
		return initVals;
	}

	private double[][][] initialValues2d(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject left = params.getObj("left_initial_values");
		DataObject right = params.getObj("right_initial_values");
		DataObject physicalConstants = params.getObj("physicalConstants");
		double gamma = physicalConstants.getDouble("gamma");
		Coordinate c = Coordinate.valueOf(Coordinate.class,
				calculationConstants.getString("coordinate"));
		int xRes = calculationConstants.getInt("xRes");
		int yRes = calculationConstants.getInt("yRes");
		double[][][] initVals = new double[xRes][yRes][8];

		double[] leftVal = new double[8];
		setCoservativeValues(left, leftVal, gamma);
		double[] rightVal = new double[8];
		setCoservativeValues(right, rightVal, gamma);

		if (c.equals(Coordinate.X))
		{
			double xRatio = physicalConstants.getDouble("xMiddlePoint") / physicalConstants
					.getDouble("xLenght");

			ArrayInitializers.relative2d().
					square(leftVal, 0, 0, xRatio, 1).
					square(rightVal, xRatio, 0, 1, 1).
					initialize(initVals);
			// ArrayInitializers.relative().
			// square(leftVal, 0, 0, 1, 1).
			// // square(rightVal, 0.4, 0.4, 0.6, 0.6).
			// fill(new ArrayInitFunction()
			// {
			//
			// @Override
			// public void init(double[] arr, double xRelative, double
			// yRelative)
			// {
			// double x = xRelative - 0.5;
			// double y = yRelative - 0.5;
			// double spotSizeSquared = 0.1;
			// double rSquared = x * x + y * y;
			// double commonMultiplier = (0.0001 / (rSquared +
			// 0.000000001))
			// * (rSquared > spotSizeSquared ? 1 : rSquared / spotSizeSquared);
			// arr[5] += x * commonMultiplier;
			// arr[6] += y * commonMultiplier;
			// }
			// }).
			// initialize(consVal);
		}
		else
		{
			double yRatio = physicalConstants.getDouble("yMiddlePoint") / physicalConstants
					.getDouble("yLenght");

			ArrayInitializers.relative2d().
					square(leftVal, 0, 0, 1, yRatio).
					square(rightVal, 0, yRatio, 1, 1).
					initialize(initVals);
		}
		return initVals;
	}

	private static void setCoservativeValues(DataObject data, double[] u, double gamma)
	{
		double rhoL = data.getDouble("rho");
		double pL = data.getDouble("p");
		double uL = data.getDouble("u");
		double vL = data.getDouble("v");
		double wL = data.getDouble("w");
		double bXL = data.getDouble("bX");
		double bYL = data.getDouble("bY");
		double bZL = data.getDouble("bZ");
		u[0] = rhoL;
		u[1] = rhoL * uL;
		u[2] = rhoL * vL;
		u[3] = rhoL * wL;
		u[4] = pL / (gamma - 1) + rhoL * (uL * uL + vL * vL + wL * wL) / 2
				+ (bYL * bYL + bZL * bZL + bXL * bXL) / 8 / PI;
		u[5] = bXL;
		u[6] = bYL;
		u[7] = bZL;
	}

}
