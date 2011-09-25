package ru.vasily.solver;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.restorator.MinmodRestorator;
import ru.vasily.solver.restorator.NoOpRestorator;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.utils.ArrayInitializers;
import ru.vasily.solver.utils.ArrayInitializers.Builder2d;
import ru.vasily.solver.utils.Func2dWrapper;
import ru.vasily.solver.utils.MagneticChargeSpotFunc;

public class MHDSolverFactory implements IMHDSolverFactory
{
	private final Map<String, Initializer> initializers = ImmutableMap
			.<String, MHDSolverFactory.Initializer> builder().
			put("fill_rect", new FillRect()).
			put("magnetic_charge_spot", new MagneticChargeSpot()).build();

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
		return new MHDSolver1D(params, restorator(params), new RoeSolverByKryukov(),
				initialValues1d(params));
	}

	private ThreePointRestorator restorator(DataObject params)
	{
		DataObject restoratorData = params.getObj("restorator");
		String type = restoratorData.getString("type");
		if ("simple".equals(type))
		{
			return new MinmodRestorator();
		}
		else if ("no_op".equals(type))
		{
			return new NoOpRestorator();
		}
		else
		{
			throw new IllegalArgumentException("unsupported restorator type:" + type);
		}
	}

	private double[][] initialValues1d(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");
		int xRes = calculationConstants.getInt("xRes");
		double gamma = physicalConstants.getDouble("gamma");
		double[][] initVals = new double[xRes][8];
		double xLength = physicalConstants
				.getDouble("xLength");
		for (DataObject initData : params.getObjects("initial_conditions_1d"))
		{
			int begin = (int) (xRes * initData.getDouble("begin") / xLength);
			int end = (int) (xRes * initData.getDouble("end") / xLength);
			for (int i = begin; i < end; i++)
			{
				double[] u = initVals[i];
				Utils.setCoservativeValues(initData.getObj("value"), u, gamma);
			}
		}
		return initVals;
	}

	private double[][][] initialValues2d(DataObject params)
	{
		DataObject calculationConstants = params.getObj("calculationConstants");
		DataObject physicalConstants = params.getObj("physicalConstants");
		int xRes = calculationConstants.getInt("xRes");
		int yRes = calculationConstants.getInt("yRes");
		double[][][] initVals = new double[xRes][yRes][8];
		Builder2d builder = ArrayInitializers.relative2d();
		for (DataObject initData : params.getObjects("initial_conditions_2d"))
		{
			initializers.get(initData.getString("type")).accept(builder, initData,
					physicalConstants);
		}
		builder.initialize(initVals);
		return initVals;
	}

	private static class FillRect implements Initializer
	{

		@Override
		public void accept(Builder2d builder, DataObject data, DataObject physicalConstants)
		{
			double[] val = new double[8];
			Utils.setCoservativeValues(data.getObj("value"), val,
					physicalConstants.getDouble("gamma"));
			builder.square(val, data.getDouble("x1"),
					data.getDouble("y1"), data.getDouble("x2"),
					data.getDouble("y2"));
		}
	}

	private static class MagneticChargeSpot implements Initializer
	{

		@Override
		public void accept(Builder2d builder, DataObject data, DataObject physicalConstants)
		{
			double xLength = physicalConstants.getDouble("xLength");
			double yLength = physicalConstants.getDouble("yLength");
			final double xSpot = data.getDouble("x");
			final double ySpot = data.getDouble("y");
			final double spot_radius = data.getDouble("radius");
			double divB = data.getDouble("divB");
			builder.fill(new Func2dWrapper(xLength, yLength,
					new MagneticChargeSpotFunc(xSpot, ySpot, spot_radius, divB)));
		}
	}

	private interface Initializer
	{
		void accept(Builder2d builder, DataObject data, DataObject physicalConstants);
	}
}
