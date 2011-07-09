package ru.vasily.solver;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static java.lang.Math.*;

public class RiemannUtilsTest {
	private static final double TOLERANCE = 0.000000001;
	private RiemannSolver solver;

	@Test
	public void without_rotation() {
		solver = solver_with_expected_input_values(1.0, 1.0);
		double[] flow = flow(1, 1);
		invoke_getFlow(values(1, 1), flow, 0);
		verify_flow(flow, 1, 1);
	}

	@Test
	public void _90_degrees_rotation() {
		solver = solver_with_expected_input_values(1.0, 0);
		double[] flow = flow(1, 0);
		invoke_getFlow(values(0, 1), flow, PI / 2);
		verify_flow(flow, 0, 1);
	}

	@Test
	public void _180_degrees_rotation() {
		solver = solver_with_expected_input_values(-1.0, 0.0);
		double[] flow = flow(1, 0);
		invoke_getFlow(values(1, 0), flow, PI);
		verify_flow(flow, -1, 0);
	}

	@Test
	public void _45_degrees_rotation() {
		double[] values = values(0, 1);
		solver = solver_with_expected_input_values(sqrt(2) / 2, sqrt(2) / 2);
		double[] flow = flow(1, 0);
		invoke_getFlow(values, flow, PI / 4);
		verify_flow(flow, sqrt(2) / 2, sqrt(2) / 2);
	}

	private void invoke_getFlow(double[] values, double[] flow, double alfa) {
		double sin_alfa = sin(alfa);
		double cos_alfa = cos(alfa);
		RiemannUtils.getFlow(solver, flow, values, values, 0, 0,
				cos_alfa, sin_alfa);
	}

	private void verify_flow(double[] flow, double ux, double uy) {
		assertThat(flow, is(tolerantlyEqualTo(flow(ux, uy))));
	}

	private double[] values(double Ux, double Uy) {
		double[] u = new double[8];
		u[1] = Ux;
		u[2] = Uy;
		double Bx = Ux;
		double By = Uy;
		u[5] = Bx;
		u[6] = By;
		return u;
	}

	private double[] flow(double rhoUx, double rhoUy) {
		double[] u = new double[8];
		u[1] = rhoUx;
		u[2] = rhoUy;
		double Bx = rhoUx;
		double By = rhoUy;
		u[5] = Bx;
		u[6] = By;
		return u;
	}

	private Matcher<double[]> tolerantlyEqualTo(final double[] array) {
		return new BaseMatcher<double[]>() {

			@Override
			public boolean matches(Object item) {
				if (!(item instanceof double[]))
				{
					return false;
				}
				double[] anotherArray = (double[]) item;
				if (anotherArray.length != array.length)
				{
					return false;
				}
				for (int i = 0; i < array.length; i++)
				{
					if (abs(anotherArray[i] - array[i]) > TOLERANCE)
						return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a array of double tolerantly equal to ").appendValue(
						Arrays.toString(array));
			}
		};
	}

	private static Matcher<Double> tolerantlyEqualTo(final Double num) {
		return new BaseMatcher<Double>() {

			@Override
			public boolean matches(Object item) {
				if (!(item instanceof Double))
				{
					return false;
				}
				double anotherDouble = (Double) item;
				if (abs(num - anotherDouble) > TOLERANCE)
					return false;
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a double tolerantly equal to ").appendValue(
						num);
			}
		};
	}

	private static RiemannSolver solver_with_expected_input_values(double ux,
			double uy) {
		return solver_with_expected_input_values(tolerantlyEqualTo(ux), tolerantlyEqualTo(uy));
	}

	private static RiemannSolver solver_with_expected_input_values(Matcher<Double> ux,
			Matcher<Double> uy) {
		return new ArgumentCheckingSolver(ux, uy);
	}

	private static class ArgumentCheckingSolver implements RiemannSolver {

		private final Matcher<Double> ux;
		private final Matcher<Double> uy;

		public ArgumentCheckingSolver(Matcher<Double> ux,
				Matcher<Double> uy) {
			this.ux = ux;
			this.uy = uy;
		}

		@Override
		public void getFlow(double[] flow, double RhoL, double UL, double VL, double WL, double PGasL, double BXL, double BYL, double BZL, double GamL, double RhoR, double UR, double VR, double WR, double PGasR, double BXR, double BYR, double BZR, double GamR) {
			assertThat("x input value", UL, this.ux);
			assertThat("y input value", VL, this.uy);

			assertThat(UR, this.ux);
			assertThat(VR, this.uy);

			assertThat(BXL, this.ux);
			assertThat(BYL, this.uy);

			assertThat(BXR, this.ux);
			assertThat(BYR, this.uy);
		}

		@Override
		public void getFlow(double[] flow, double[] uL, double[] uR, double gammaL, double gammaR) {

			throw new RuntimeException("method is not implemented");
		}
	}
}
