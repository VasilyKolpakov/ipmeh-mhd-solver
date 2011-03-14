package ru.vasily.solver;

public class FlowRestorator {
	private final Fetcher fetcher;
	private final double nu;
	private final double omega;
	private final double[][] temp;

	public FlowRestorator(Fetcher fetcher, double nu, double omega, int varNum) {
		this.nu = nu;
		this.fetcher = fetcher;
		this.omega = omega;
		this.temp = new double[20][varNum];
	}

	public void setRestoredURandUL(double[] uR, double[] uL, int i) {
		double[] delta_wave_plus_5_2 = temp[0];
		double[] delta_d_wave_plus_3_2 = temp[1];
		double[] delta_d_wave_minus_half = temp[2];
		double[] delta_wave_plus_half = temp[3];

		set_wave_delta(delta_wave_plus_5_2, i + 2);
		set_wave_delta(delta_wave_plus_half, i);
		set_d_wave_delta(delta_d_wave_plus_3_2, i + 1);
		set_d_wave_delta(delta_d_wave_minus_half, i - 1);

		mult(delta_wave_plus_5_2, delta_wave_plus_5_2, -0.25 * (1 - nu));
		mult(delta_d_wave_plus_3_2, delta_d_wave_plus_3_2, -0.25 * (1 + nu));
		mult(delta_d_wave_minus_half, delta_d_wave_minus_half, 0.25 * (1 - nu));
		mult(delta_wave_plus_half, delta_wave_plus_half, 0.25 * (1 + nu));

		fetcher.setU(uR, i + 1);
		fetcher.setU(uL, i);

		add(uR, uR, delta_wave_plus_5_2);
		add(uR, uR, delta_d_wave_plus_3_2);

		add(uL, uL, delta_d_wave_minus_half);
		add(uL, uL, delta_wave_plus_half);
	}

	private void set_wave_delta(double[] result, int i) {
		double[] delta_plus_half = temp[4];
		double[] delta_minus_half = temp[5];
		fetcher.setDelta(delta_plus_half, i);
		fetcher.setDelta(delta_minus_half, i - 1);
		mult(delta_minus_half, delta_minus_half, omega);
		minmod(result, delta_plus_half, delta_minus_half);
	}

	private void set_d_wave_delta(double[] result, int i) {
		double[] delta_plus_half = temp[6];
		double[] delta_plus_3_2 = temp[7];
		fetcher.setDelta(delta_plus_half, i);
		fetcher.setDelta(delta_plus_3_2, i + 1);
		mult(delta_plus_3_2, delta_plus_3_2, omega);
		minmod(result, delta_plus_half, delta_plus_3_2);
	}

	private void mult(double[] result, double[] a, double mult) {
		if (result.length != a.length)
			throw new IllegalArgumentException("sizes do not match");
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] * mult;
		}
	}

	private void add(double[] result, double[] a, double[] b) {
		if (result.length != a.length | a.length != b.length)
			throw new IllegalArgumentException("sizes do not match");
		for (int i = 0; i < b.length; i++) {
			result[i] = a[i] + b[i];
		}
	}

	private void minmod(double[] result, double[] a, double[] b) {
		if (result.length != a.length | a.length != b.length)
			throw new IllegalArgumentException("sizes do not match");
		for (int i = 0; i < b.length; i++) {
			result[i] = 0.5 * (Math.signum(a[i]) + Math.signum(b[i]))
					* Math.min(Math.abs(a[i]), Math.abs(b[i]));
		}
	}

	public interface Fetcher {
		void setU(double[] arr, int i);

		void setDelta(double[] arr, int i);
	}
}
