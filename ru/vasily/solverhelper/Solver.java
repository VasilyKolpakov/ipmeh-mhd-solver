package ru.vasily.solverhelper;

import java.util.Map;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObj;
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
	public CalculationResult solve(Parameters p) {
		MHDSolver solver = new MHDSolver(p);
		try {
			while (solver.getTotalTime() < p.physicalConstants.totalTime) {
				solver.nextTimeStep();
			}
		} catch (AlgorithmError err) {
			StringBuilder sb = new StringBuilder();
			serializer.writeObject(err.getParams(), sb);
			ImmutableList<DataObj> emptyIterable = ImmutableList.of();
			CalculationResult calculationResult = new CalculationResult(
					emptyIterable, sb.toString());
			return calculationResult;
		}
		ImmutableMap<String, double[]> data = solver.getData();
		double[] xCoord = solver.getXCoord();
		ImmutableMap<String, String> logData = solver.getLogData();
		CalculationResult calculationResult = createSuccessCalculationResult(
				data, xCoord, logData);
		return calculationResult;
	}

	private CalculationResult createSuccessCalculationResult(ImmutableMap<String, double[]> data, double[] xCoord, Map<String, String> logData) {
		double min_x = ArrayUtils.min(xCoord);
		double max_x = ArrayUtils.max(xCoord);
		Map<String, String> commonProps = ImmutableMap.of(DataObj.MIN_X,
				String.valueOf(min_x), DataObj.MAX_X, String.valueOf(max_x));
		Builder<DataObj> listBuilder = ImmutableList.builder();
		for (String key : data.keySet()) {
			double[] valueArray = data.get(key);
			DataObj dataObj = createDataObj(key, valueArray, xCoord,
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

	private DataObj createDataObj(String key, double[] valueArray, double[] xCoord, Map<String, String> commonProps) {
		ImmutableMap.Builder<String, String> propertiesBuilder = ImmutableMap
				.builder();
		propertiesBuilder.putAll(commonProps);
		propertiesBuilder.put(DataObj.VALUE_NAME, key);
		propertiesBuilder.put(DataObj.MIN_Y,
				String.valueOf(ArrayUtils.min(valueArray)));
		propertiesBuilder.put(DataObj.MAX_Y,
				String.valueOf(ArrayUtils.max(valueArray)));
		DataObj dataObj = new DataObj(key, valueArray, xCoord,
				propertiesBuilder.build());
		return dataObj;
	}
}
