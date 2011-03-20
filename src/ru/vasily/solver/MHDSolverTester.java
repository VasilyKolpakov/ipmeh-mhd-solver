package ru.vasily.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractList;

import com.google.common.base.Function;

import ru.vasily.dataobjs.Parameters;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.AppConfig;
import ru.vasily.solverhelper.misc.ISerializer;

public class MHDSolverTester {

	private final ISerializer serializer;

	public MHDSolverTester(ISerializer serializer) {
		this.serializer = serializer;
	}

	public static String pressureToString(final MHDSolver solver) {

		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return String.format("%f", solver.getPressure(index));
			}

			@Override
			public int size() {
				return solver.xRes;
			}
		}.toString();
	}

	public static String pressurePrToString(final MHDSolver solver) {

		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return String.format("%f", solver.getPressurePr(index));
			}

			@Override
			public int size() {
				return solver.xRes;
			}
		}.toString();
	}

	private void runTest() throws FileNotFoundException, IOException {
		File fileParam = new File("input/jcp115-485_Fig_7.js");
		Parameters param = serializer.readObject(new FileReader(fileParam),
				Parameters.class);
		int xRes = param.calculationConstants.xRes;
		final MHDSolver solver = new MHDSolver(param);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!in.readLine().equals("end")) {
			solver.nextTimeStep();
			System.out.println(solver.getTotalTime());
			System.out.println(funcToString(xRes, new GetPressureFunc(solver)));
		}
		MHDSolverTester.pressurePrToString(solver);

	}

	private String funcToString(final int size, final Function<Integer, Number> func) {
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return String.format("%f", func.apply(index));
			}

			@Override
			public int size() {
				return size;
			}
		}.toString();
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		MHDSolverTester test = new MyDI(new AppConfig())
				.getInstanceViaDI(MHDSolverTester.class);
		test.runTest();
	}

	private static final class GetPressureFunc implements
			Function<Integer, Number> {
		private final MHDSolver solver;

		private GetPressureFunc(MHDSolver solver) {
			this.solver = solver;
		}

		@Override
		public Number apply(Integer input) {
			return solver.getPressure(input);
		}
	}

	private static final class GetPressurePrFunc implements
			Function<Integer, Number> {
		private final MHDSolver solver;

		private GetPressurePrFunc(MHDSolver solver) {
			this.solver = solver;
		}

		@Override
		public Number apply(Integer input) {
			return solver.getPressurePr(input);
		}
	}

}
