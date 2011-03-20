package ru.vasily.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObj;
import ru.vasily.dataobjs.Parameters;
import ru.vasily.solverhelper.ApplicationMain;
import ru.vasily.solverhelper.IParamsLoader;
import ru.vasily.solverhelper.IResultWriter;
import ru.vasily.solverhelper.ISolver;
import ru.vasily.solverhelper.Solver;
import ru.vasily.solverhelper.tecplot.ITecplotManager;

public class ApplicationMainTest {
	private static final class ITecplotManagerImplementation implements
			ITecplotManager {
		@Override
		public void runMacro(File macro) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void runMacro(Iterable<File> macro)
				throws IOException {
			// TODO Auto-generated method stub
			
		}
	}

	private static final class IResultWriterImplementation implements
			IResultWriter {
		@Override
		public void createResultDir(File path, CalculationResult result,
				File templateDir) throws IOException {
			System.out.println("creating result dir " + path.getAbsolutePath()
					+ "\n for result = " + result + "\n, using template dir "
					+ templateDir.getAbsolutePath());
		}

		@Override
		public void createResultDir(File path, CalculationResult result)
				throws IOException {
		}
	}

	private static final class ISolverImplementation implements ISolver {
		@Override
		public CalculationResult solve(Parameters p) {
			CalculationResult calculationResult = new CalculationResult(
					Collections.singletonList(new DataObj()),"");
			System.out.println("solver start");
			System.out.println("getting result " + calculationResult);
			return calculationResult;
		}
	}

	private static final class IParamsLoaderImplementation implements
			IParamsLoader {
		@Override
		public Parameters getParams(File file) throws IOException {
			System.out.println("loading param");
			return new Parameters();
		}
	}

	static void testExecute() {
		ApplicationMain test = new ApplicationMain(
				new IParamsLoaderImplementation(), new ISolverImplementation(),
				new IResultWriterImplementation());
		test.execute("input\\", "output\\", "template\\");
	}

	public static void main(String[] args) {
		testExecute();
	}
}
