package ru.vasily.solver;

import com.google.common.collect.ImmutableMap;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.border.Array2dBorderConditions;
import ru.vasily.solver.border.ContinuationCondition;
import ru.vasily.solver.border.PeriondicConditions;
import ru.vasily.solver.initialcond.*;
import ru.vasily.solver.restorator.MinmodRestorator;
import ru.vasily.solver.restorator.NoOpRestorator;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RiemannSolver1Dto2DWrapper;
import ru.vasily.solver.riemann.RoeSolverByKryukov;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class MHDSolverFactory implements IMHDSolverFactory
{
	private final Map<String, Initializer> initializers = ImmutableMap
			.<String, MHDSolverFactory.Initializer>builder().
					put("fill_rect", new FillRect()).
					put("fill_circle", new FillCircle()).
					put("magnetic_charge_spot", new MagneticChargeSpot()).
					put("rotor_problem", new RotorProblem()).
					build();
	private final Map<String, BorderConditionsFactory> borderConditions = ImmutableMap
			.<String, BorderConditionsFactory>builder().
					put("continuation", new ContinuationBCF()).
					put("periodic", new PeriodicBCF()).
					build();

	@Override
	public MHDSolver createSolver(DataObject params)
	{
		String solverType = params.getString("solver");
		if (solverType.equalsIgnoreCase("1d"))
		{
			return solver1d(params);
		} else if (solverType.equalsIgnoreCase("2d"))
		{
			return solver2d(params);
		}
		throw new RuntimeException("incorrect solver type : " + solverType);
	}

	private MHDSolver2D solver2d(DataObject params)
	{
		return new MHDSolver2D(params, restorator(params), new RiemannSolver1Dto2DWrapper(
				new RoeSolverByKryukov()),
				borderConditions(params),
				initialValues2d(params));
	}

	private Array2dBorderConditions borderConditions(DataObject params)
	{
		String type = params.getObj("border_conditions").getString("type");
		BorderConditionsFactory factory = borderConditions.get(type);
		checkNotNull(factory, "not supported border conditions type '%s', supported are %s", type, borderConditions.keySet());
		return factory.createConditions(params);
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
		if ("simple_minmod".equals(type))
		{
			return new MinmodRestorator();
		} else if ("no_op".equals(type))
		{
			return new NoOpRestorator();
		} else
		{
			throw new IllegalArgumentException("unsupported restorator type:" + type + "only simple_minmod and no_op are supported ");
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
		double xLength = physicalConstants.getDouble("xLength");
		double yLength = physicalConstants.getDouble("yLength");

		InitialValues2dBuilder<double[][][]> builder_ = new Array2dFiller(xRes, yRes, xLength, yLength);
		for (DataObject initData : params.getObjects("initial_conditions_2d"))
		{
			initializers.get(initData.getString("type")).accept(builder_, initData,
					physicalConstants);
		}
		return builder_.build();
	}

	private static class FillRect implements Initializer
	{

		@Override
		public void accept(InitialValues2dBuilder<?> builder, DataObject data, DataObject physicalConstants)
		{
			double[] val = parseConservativeVals(data, physicalConstants);
			builder.apply(new FillSquareFunction(val, data.getDouble("x1"),
					data.getDouble("y1"), data.getDouble("x2"),
					data.getDouble("y2")));
		}

	}

	private static class MagneticChargeSpot implements Initializer
	{

		@Override
		public void accept(InitialValues2dBuilder<?> builder, DataObject data, DataObject physicalConstants)
		{
			final double xSpot = data.getDouble("x");
			final double ySpot = data.getDouble("y");
			final double spot_radius = data.getDouble("radius");
			double divB = data.getDouble("divB");
			builder.apply(new MagneticChargeSpotFunc(xSpot, ySpot, spot_radius, divB));
		}
	}

	private static class FillCircle implements Initializer
	{

		@Override
		public void accept(InitialValues2dBuilder<?> builder, DataObject data, DataObject physicalConstants)
		{
			double[] val = parseConservativeVals(data, physicalConstants);
			final double x = data.getDouble("x");
			final double y = data.getDouble("y");
			final double radius = data.getDouble("radius");
			builder.apply(new FillCircleFunction(val, x, y, radius));
		}
	}

	private static class RotorProblem implements Initializer
	{

		@Override
		public void accept(InitialValues2dBuilder<?> builder, DataObject data, DataObject physicalConstants)
		{
			builder.apply(new RotorProblemFunction(data, physicalConstants.getDouble("gamma")));
		}
	}

	private static double[] parseConservativeVals(DataObject data, DataObject physicalConstants)
	{
		double[] val = new double[8];
		Utils.setCoservativeValues(data.getObj("value"), val,
				physicalConstants.getDouble("gamma"));
		return val;
	}

	private interface Initializer
	{
		void accept(InitialValues2dBuilder<?> builder, DataObject data, DataObject physicalConstants);
	}

	private interface BorderConditionsFactory
	{
		Array2dBorderConditions createConditions(DataObject params);
	}

	private class ContinuationBCF implements BorderConditionsFactory
	{
		@Override
		public Array2dBorderConditions createConditions(DataObject allParams)
		{
			DataObject calculationConstants = allParams.getObj("calculationConstants");
			int xRes = calculationConstants.getInt("xRes");
			int yRes = calculationConstants.getInt("yRes");
			return new ContinuationCondition(ContinuationCondition.Location.All, xRes, yRes);
		}
	}

	private class PeriodicBCF implements BorderConditionsFactory
	{
		@Override
		public Array2dBorderConditions createConditions(DataObject allParams)
		{
			DataObject calculationConstants = allParams.getObj("calculationConstants");
			int xRes = calculationConstants.getInt("xRes");
			int yRes = calculationConstants.getInt("yRes");
			return new PeriondicConditions(xRes, yRes);
		}
	}
}
