package ru.vasily.test;

import java.util.Arrays;

import com.google.common.base.Objects;

import ru.vasily.solver.FlowRestorator;

public class FlowRestoratorTest {

	public static void main(String[] args) {
		double[] input = new double[] { 1.0, 2.0, 3.0, 5.0, 100, 0.5, 3, 5, 78 };

		FlowRestorator.Fetcher fetcher = getFetcher(input);
		FlowRestorator restorator = new FlowRestorator(fetcher, 1, 1, 1);
		double[] resultL = new double[input.length];
		double[] resultR = new double[input.length];
		setResult(restorator, resultL, resultR);
		String out = Objects.toStringHelper("result")
				.add("resultL", Arrays.toString(resultL))
				.add("resultR", Arrays.toString(resultR)).toString();
		System.out.println(out);
	}

	private static void setResult(FlowRestorator restorator, double[] resultL, double[] resultR) {
		double[] ul = new double[1];
		double[] ur = new double[1];
		for (int i = 0; i < resultL.length; i++) {
			try {
				restorator.setRestoredURandUL(ur, ul, i);
				resultL[i] = ul[0];
				resultR[i] = ur[0];
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}
	}

	private static FlowRestorator.Fetcher getFetcher(final double[] array) {
		FlowRestorator.Fetcher fetcher = new FlowRestorator.Fetcher() {

			@Override
			public void setU(double[] arr, int i) {
				arr[0] = array[i];
			}

			@Override
			public void setDelta(double[] arr, int i) {
				arr[0] = array[i + 1] - array[i];
			}
		};
		return fetcher;
	}
}
