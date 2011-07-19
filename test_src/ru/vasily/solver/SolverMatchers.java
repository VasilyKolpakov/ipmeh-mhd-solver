package ru.vasily.solver;

import static java.lang.Math.abs;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SolverMatchers {
	private static final double TOLERANCE = 0.000000001;

	public static Matcher<double[]> tolerantlyEqualTo(final double[] array) {
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

	public static Matcher<Double> tolerantlyEqualTo(final Double num) {
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

}
