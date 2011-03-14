package ru.vasily.test;

import java.util.Arrays;

import ru.vasily.solver.FlowRestorator;

public class FlowRestoratorTest {

	public static void main(String[] args) {
		FlowRestorator.Fetcher fetcher = new FlowRestorator.Fetcher() {

			@Override
			public void setU(double[] arr, int i) {
				arr[0] = 1;
			}

			@Override
			public void setDelta(double[] arr, int i) {
				arr[0] = 0;
			}
		};
		FlowRestorator restorator = new FlowRestorator(fetcher, 1, 1, 1);
		double[] ul = new double[1];
		double[] ur = new double[1];
		restorator.setRestoredURandUL(ur, ul, 0);
		System.out.println(Arrays.toString(ul) + " " + Arrays.toString(ur));
	}
}
