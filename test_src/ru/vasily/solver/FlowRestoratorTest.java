package ru.vasily.solver;

import junit.framework.Assert;

import org.junit.Test;

import ru.vasily.solver.FlowRestorator.Fetcher;

public class FlowRestoratorTest {
	private double[] data;
	private double[] uR = new double[1];
	private double[] uL = new double[1];
	private double[] data_minus;
	private double[] uR_minus = new double[1];
	private double[] uL_minus = new double[1];

	@Test
	public void linear() {
		data(0, 1, 2, 3);
		restore_with_etta_and_omega(0, 1);
		checkResult(1.5, 1.5);
	}

	@Test
	public void non_linear_right() {
		data(0, 1, 2, 100);
		restore_with_etta_and_omega(0, 1);
		checkResult(1.5, 1.5);
	}

	@Test
	public void non_linear_left() {
		data(-100, 1, 2, 3);
		restore_with_etta_and_omega(0, 1);
		checkResult(1.5, 1.5);
	}

	@Test
	public void local_min_left() {
		data(2, 1, 2, 3);
		restore_with_etta_and_omega(0, 1);
		checkResult(1.0, 1.5);
	}

	@Test
	public void local_min_right() {
		data(0, 1, 2, 1);
		restore_with_etta_and_omega(0, 1);
		checkResult(1.5, 2.0);
	}

	private void restore_with_etta_and_omega(double etta, double omega) {
		restorator(etta, omega,data).setRestoredURandUL(uR, uL, 1);
		restorator(etta, omega,data_minus).setRestoredURandUL(uR_minus, uL_minus, 1);
	}

	private void data(double... data) {
		this.data = data;
		this.data_minus = new double[data.length];
		for (int i = 0; i < data.length; i++)
		{
			data_minus[i] = -data[i];
		}
	}

	private void checkResult(double expectedUL, double expectedUR) {
		Assert.assertEquals("uR_minus", -expectedUR, uR_minus[0], 0.000001);
		Assert.assertEquals("uL_minus", -expectedUL, uL_minus[0], 0.000001);
		Assert.assertEquals("uR", expectedUR, uR[0], 0.000001);
		Assert.assertEquals("uL", expectedUL, uL[0], 0.000001);
	}

	private FlowRestorator restorator(double nu, double omega,final double [] data) {
		FlowRestorator flowRestorator = new FlowRestorator(new Fetcher() {

			@Override
			public void setU(double[] arr, int i) {
				arr[0] = data[i];
			}

			@Override
			public void setDelta(double[] arr, int i) {
				arr[0] = data[i + 1] - data[i];
			}
		}, nu, omega, 1);
		return flowRestorator;
	}
}
