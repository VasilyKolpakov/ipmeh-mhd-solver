package ru.vasily.solverhelper;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.ArrayDataObj;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.Parameters;
import ru.vasily.solver.AlgorithmError;
import ru.vasily.solver.MHDSolver;
import ru.vasily.solverhelper.misc.ArrayUtils;
import ru.vasily.solverhelper.misc.ISerializer;

public class Solver implements ISolver {

	private final ISerializer serializer;

	public Solver(ISerializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public CalculationResult solve(DataObject p) {
		MHDSolver solver = new MHDSolver(p);
		return calculate(solver,
				iterateWithTimeLimit(solver, p.getObj("physicalConstants").getDouble("totalTime")));
	}

	private CalculationResult calculate(MHDSolver solver, Runnable calcTask) {
		try
		{
			calcTask.run();
		} catch (AlgorithmError err)
		{
			StringBuilder sb = new StringBuilder();
			serializer.writeObject(err.getParams(), sb);
			CalculationResult calculationResult = new CalculationResult(
							ImmutableList.<ArrayDataObj> of(),
							sb.toString()
							);
			return calculationResult;
		}
		ImmutableMap<String, double[]> data = solver.getData();
		double[] xCoord = solver.getXCoord();
		ImmutableMap<String, String> logData = solver.getLogData();
		CalculationResult calculationResult =
				createSuccessCalculationResult(data, xCoord, logData);
		return calculationResult;
	}

	@Override
	public IterativeSolver getSolver(final DataObject p) {
		return new IterativeSolver() {
			private MHDSolver solver = new MHDSolver(p);

			@Override
			public CalculationResult next(int iterations) {
				return calculate(solver,
						iterateWithCountLimit(solver, iterations));
			}

		};
	}

	private Runnable iterateWithCountLimit(final MHDSolver solver,
			final int limit) {
		return new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < limit; i++)
				{
					solver.nextTimeStep();
				}
			}
		};
	}

	private CalculationResult createSuccessCalculationResult(
			ImmutableMap<String, double[]> data, double[] xCoord,
			Map<String, String> logData) {
		double min_x = ArrayUtils.min(xCoord);
		double max_x = ArrayUtils.max(xCoord);
		Map<String, String> commonProps = ImmutableMap.of(
				ArrayDataObj.MIN_X, String.valueOf(min_x),
				ArrayDataObj.MAX_X, String.valueOf(max_x)
				);
		Builder<ArrayDataObj> listBuilder = ImmutableList.builder();
		for (String key : data.keySet())
		{
			double[] valueArray = data.get(key);
			ArrayDataObj dataObj = createDataObj(key, valueArray, xCoord,
					commonProps);
			listBuilder.add(dataObj);
		}
		StringBuilder sb = new StringBuilder();
		serializer.writeObject(logData, sb);

		CalculationResult calculationResult = new CalculationResult(
				listBuilder.build(), "calculation done \n log = "
						+ sb.toString());
		return calculationResult;
	}

	private ArrayDataObj createDataObj(String key, double[] valueArray,
			double[] xCoord, Map<String, String> commonProps) {
		ImmutableMap.Builder<String, String> propertiesBuilder = ImmutableMap
				.builder();
		propertiesBuilder.putAll(commonProps);
		propertiesBuilder.put(ArrayDataObj.VALUE_NAME, key);
		propertiesBuilder.put(ArrayDataObj.MIN_Y,
				String.valueOf(ArrayUtils.min(valueArray)));
		propertiesBuilder.put(ArrayDataObj.MAX_Y,
				String.valueOf(ArrayUtils.max(valueArray)));
		ArrayDataObj dataObj = new ArrayDataObj(key, valueArray, xCoord,
				propertiesBuilder.build());
		return dataObj;
	}

	private static Runnable iterateWithTimeLimit(final MHDSolver solver,
			final double totalTime) {
		return new Runnable() {
			@Override
			public void run() {
				while (solver.getTotalTime() < totalTime)
				{
					solver.nextTimeStep();
				}
			}
		};
	}
}
