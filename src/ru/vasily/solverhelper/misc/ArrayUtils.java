package ru.vasily.solverhelper.misc;

public class ArrayUtils {
	public static double max(double arr[]) {
		double max = Double.NEGATIVE_INFINITY;
		for (double d : arr) {
			max = Math.max(max, d);
		}
		return max;
	}

	public static double min(double arr[]) {
		double min = Double.POSITIVE_INFINITY;
		for (double d : arr) {
			min = Math.min(min, d);
		}
		return min;
	}

}
