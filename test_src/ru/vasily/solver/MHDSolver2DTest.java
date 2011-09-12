package ru.vasily.solver;

import static org.junit.Assert.*;
import static java.lang.Math.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.dataobjs.JacksonDataObjService;
import ru.vasily.solverhelper.PlotDataVisitor;

public class MHDSolver2DTest
{
	@Test
	public void test()
	{
		DataObject data = data(new File(
				"d:\\development\\kafedra_prj\\SolverHelper\\input\\jcp115-485_Fig_7.js"));
		MHDSolver solver = new MHDSolver2D(data, new LoggingSolver2D());
		solver.nextTimeStep();
	}

	private void printArray(double[] x)
	{
		System.out.println(Arrays.toString(x));
	}

	@Test
	public void simple_X_Y_comparing()
	{

		DataObject dataX = data("X", 2, 1, 2, 1);
		DataObject dataY = data("Y", -1, 2, -1, 2);
		assertXYEqual(dataX, dataY);
	}

	@Test
	public void simple_1D_2D_comparing()
	{

		DataObject data = data("X", 2, 1, 2, 1);
		assert1D2DEqual(data);
	}

	@Test
	public void not_simple_X_Y_comparing()
	{
		assertXYEqual(problemDataX(), problemDataY());
	}

	@Test
	public void not_simple_1D_2D_comparing()
	{
		assert1D2DEqual(problemDataX());
	}

	private void assert1D2DEqual(DataObject data)
	{
		double rhoX_1d = getOutput1D(data)[1];
		double rhoX = getOutput2D(data)[1];
		assertTrue("x_1d and x_2d are equal", abs(rhoX_1d - rhoX) < 0.00000000001);
	}

	private void assertXYEqual(DataObject dataX, DataObject dataY)
	{
		double rhoX = getOutput2D(dataX)[1];
		double rhoY = getOutput2D(dataY)[1];
		assertTrue("x and y are symmetrical", abs(rhoX - rhoY) < 0.0000000000001);
	}

	private double[] getOutput2D(DataObject data)
	{
		MHDSolver2D solver = new MHDSolver2D(data, new RiemannSolver1Dto2DWrapper(
				new RoeSolverByKryukov()));
		return get_data_after_first_timestep(solver).get("density");
	}

	private double[] getOutput1D(DataObject data)
	{
		MHDSolver solver = new MHDSolver1D(data, new RoeSolverByKryukov());
		return get_data_after_first_timestep(solver).get("density");
	}

	private Map<String, double[]> get_data_after_first_timestep(MHDSolver solver)
	{
		Map<String, double[]> ret = new HashMap<String, double[]>();
		solver.nextTimeStep();
		solver.getData().visit(new DataGetter(ret));
		return ret;
	}

	private static DataObject data(File file)
	{
		try
		{
			return new JacksonDataObjService().readObject(new FileReader(file));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static DataObject data(String coordinate, double uL, double vL, double bXL, double bYl)
	{
		Map<String, Object> calculationConstants = ImmutableMap.<String, Object> builder()
				.put("CFL", 0.25)
				.put("xRes", 3)
				.put("yRes", 3)
				.put("omega", 1.0)
				.put("nu", 0.0)
				.put("coordinate", coordinate)
				.build();
		Map<String, Object> physicalConstants = ImmutableMap.<String, Object> builder()
				.put("gamma", 1.6666666666)
				.put("xLenght", 1.0)
				.put("yLenght", 1.0)
				.put("xMiddlePoint", 0.5)
				.put("yMiddlePoint", 0.5)
				.put("totalTime", 0.02)
				.build();
		Map<String, Object> left_initial_values = ImmutableMap.<String, Object> builder()
				.put("rho", 0.2)
				.put("p", 0.2)
				.put("u", uL)
				.put("v", vL)
				.put("w", 0)
				.put("bX", bXL)
				.put("bY", bYl)
				.put("bZ", 0.0)
				.build();
		Map<String, Object> right_initial_values = ImmutableMap.<String, Object> builder()
				.put("rho", 0.1)
				.put("p", 0.1)
				.put("u", 0)
				.put("v", 0)
				.put("w", 0)
				.put("bX", 0.0)
				.put("bY", 0.0)
				.put("bZ", 0.0)
				.build();

		Map<String, Object> data = ImmutableMap.<String, Object> builder()
				.put("calculationConstants", calculationConstants)
				.put("physicalConstants", physicalConstants)
				.put("left_initial_values", left_initial_values)
				.put("right_initial_values", right_initial_values)
				.build();
		return new MapDataObject(data);
	}

	private DataObject problemDataX()
	{
		String json = "{\r\n" +
				"  \"calculationConstants\" : {\r\n" +
				"    \"CFL\" : 0.25,\r\n" +
				"    \"xRes\" : 3,\r\n" +
				"    \"yRes\" : 3,\r\n" +
				"    \"omega\" : 1.0,\r\n" +
				"    \"nu\" : 0.0,\r\n" +
				"	\"coordinate\" : \"X\"\r\n" +
				"  },\r\n" +
				"  \"physicalConstants\" : {\r\n" +
				"    \"gamma\" : 1.6666666666,\r\n" +
				"    \"xLenght\" : 1.0,\r\n" +
				"    \"yLenght\" : 1.0,\r\n" +
				"    \"xMiddlePoint\" : 0.5,\r\n" +
				"    \"yMiddlePoint\" : 0.5,\r\n" +
				"    \"totalTime\" : 0.04\r\n" +
				"	},\r\n" +
				"  \"left_initial_values\" : {\r\n" +
				"    \"rho\" : 0.15,\r\n" +
				"    \"p\" : 0.28,\r\n" +
				"    \"u\" : 21.55,\r\n" +
				"    \"v\" : 1,\r\n" +
				"    \"w\" : 1,\r\n" +
				"    \"bX\" : 0.0,\r\n" +
				"    \"bY\" : -2.0,\r\n" +
				"    \"bZ\" : -1.0\r\n" +
				"  },\r\n" +
				"  \"right_initial_values\" : {\r\n" +
				"    \"rho\" : 0.1,\r\n" +
				"    \"p\" : 0.1,\r\n" +
				"    \"u\" : -26.45,\r\n" +
				"    \"v\" : 0.0,\r\n" +
				"    \"w\" : 0.0,\r\n" +
				"    \"bX\" : 0.0,\r\n" +
				"    \"bY\" : 2.0,\r\n" +
				"    \"bZ\" : 1.0\r\n" +
				"  }\r\n" +
				"}";
		return readJson(json);
	}

	private DataObject problemDataY()
	{
		String json = "{\r\n" +
				"  \"calculationConstants\" : {\r\n" +
				"    \"CFL\" : 0.25,\r\n" +
				"    \"xRes\" : 3,\r\n" +
				"    \"yRes\" : 3,\r\n" +
				"    \"omega\" : 1.0,\r\n" +
				"    \"nu\" : 0.0,\r\n" +
				"	\"coordinate\" : \"Y\"\r\n" +
				"  },\r\n" +
				"  \"physicalConstants\" : {\r\n" +
				"    \"gamma\" : 1.6666666666,\r\n" +
				"    \"xLenght\" : 1.0,\r\n" +
				"    \"yLenght\" : 1.0,\r\n" +
				"    \"xMiddlePoint\" : 0.5,\r\n" +
				"    \"yMiddlePoint\" : 0.5,\r\n" +
				"    \"totalTime\" : 0.04\r\n" +
				"	},\r\n" +
				"  \"left_initial_values\" : {\r\n" +
				"    \"rho\" : 0.15,\r\n" +
				"    \"p\" : 0.28,\r\n" +
				"    \"u\" : -1,\r\n" +
				"    \"v\" : 21.55,\r\n" +
				"    \"w\" : 1,\r\n" +
				"    \"bX\" : 2.0,\r\n" +
				"    \"bY\" : 0.0,\r\n" +
				"    \"bZ\" : -1.0\r\n" +
				"  },\r\n" +
				"  \"right_initial_values\" : {\r\n" +
				"    \"rho\" : 0.1,\r\n" +
				"    \"p\" : 0.1,\r\n" +
				"    \"u\" : 0.0,\r\n" +
				"    \"v\" : -26.45,\r\n" +
				"    \"w\" : 0.0,\r\n" +
				"    \"bX\" : -2.0,\r\n" +
				"    \"bY\" : 0.0,\r\n" +
				"    \"bZ\" : 1.0\r\n" +
				"  }\r\n" +
				"}";
		return readJson(json);
	}

	private DataObject readJson(String json)
	{
		try
		{
			return new JacksonDataObjService().readObject(new StringReader(json));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static class MapDataObject implements DataObject
	{
		private final Map<String, Object> data;

		public MapDataObject(Map<String, Object> data)
		{
			this.data = data;
		}

		@Override
		public double getDouble(String valueName)
		{
			return ((Number) data.get(valueName)).doubleValue();
		}

		@Override
		public int getInt(String valueName)
		{
			return ((Number) data.get(valueName)).intValue();
		}

		@Override
		public DataObject getObj(String valueName)
		{
			return new MapDataObject((Map<String, Object>) data.get(valueName));
		}

		@Override
		public String getString(String valueName)
		{
			return (String) data.get(valueName);
		}
	}

	private static class DataGetter implements PlotDataVisitor
	{
		private final Map<String, double[]> data;

		public DataGetter(Map<String, double[]> data)
		{
			this.data = data;
		}

		@Override
		public void handleResult1D(String name, double[] x, double[] y)
		{
			data.put(name, y);
		}
	}

	private class LoggingSolver1D implements RiemannSolver
	{

		private final RiemannSolver realRiemannSolver = new RoeSolverByKryukov();

		@Override
		public void getFlow(double[] flow, double RhoL, double UL, double VL, double WL, double PGasL, double BXL, double BYL, double BZL, double GamL, double RhoR, double UR, double VR, double WR, double PGasR, double BXR, double BYR, double BZR, double GamR)
		{
			double[] ul = new double[8];
			ul[0] = RhoL;
			ul[1] = UL;
			ul[2] = VL;
			ul[3] = WL;
			ul[4] = PGasL;
			ul[5] = BXL;
			ul[6] = BYL;
			ul[7] = BZL;
			double[] ur = new double[8];
			ur[0] = RhoR;
			ur[1] = UR;
			ur[2] = VR;
			ur[3] = WR;
			ur[4] = PGasR;
			ur[5] = BXR;
			ur[6] = BYR;
			ur[7] = BZR;
			System.out.println("calculating flow " + Arrays.toString(ul) + Arrays.toString(ur));
			realRiemannSolver.getFlow(flow, RhoL, UL, VL, WL, PGasL, BXL, BYL, BZL, GamL, RhoR, UR,
					VR, WR, PGasR, BXR, BYR, BZR, GamR);
		}

		@Override
		public void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR)
		{
			System.out.println("calculating flow " + Arrays.toString(uL) + Arrays.toString(uR));
			realRiemannSolver.getFlow(flow, uL, uR, gammaL, gammaR);
		}
	}

	private class LoggingSolver2D implements RiemannSolver2D
	{

		RiemannSolver2D riemannSolver2D = new RiemannSolver1Dto2DWrapper(new RoeSolverByKryukov());

		@Override
		public void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR, double cos_alfa, double sin_alfa)
		{
			riemannSolver2D.getFlow(flow, uL, uR, gammaL, gammaR, cos_alfa, sin_alfa);
			if (sin_alfa == 1.0)
				System.out.println("LoggingSolver2D :\n" +
						"flow = " + Arrays.toString(flow) + "\n" +
						"ul = " + Arrays.toString(uL) + "\n" +
						"ur = " + Arrays.toString(uR) + "\n" +
						"cos_alfa = " + cos_alfa + " sin_alfa = " + sin_alfa);
		}
	}
}
