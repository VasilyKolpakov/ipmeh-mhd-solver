package ru.vasily.solverhelper.misc;

public class ArrayUtils
{
	public static double max(double arr[])
	{
		double max = Double.NEGATIVE_INFINITY;
		for (double d : arr)
		{
			max = Math.max(max, d);
		}
		return max;
	}

	public static double max(double arr[][])
	{
		double max = Double.NEGATIVE_INFINITY;
		for (double[] innerArray : arr)
		{
			max = Math.max(max, max(innerArray));
		}
		return max;
	}

	public static double min(double arr[])
	{
		double min = Double.POSITIVE_INFINITY;
		for (double d : arr)
		{
			min = Math.min(min, d);
		}
		return min;
	}

	public static double min(double arr[][])
	{
		double min = Double.POSITIVE_INFINITY;
		for (double[] innerArray : arr)
		{
			min = Math.min(min, min(innerArray));
		}
		return min;
	}

	public static boolean isNAN(double[] arr)
	{
		for (int i = 0; i < arr.length; i++)
		{
			if (Double.isNaN(arr[i]))
				return true;
		}
		return false;
	}

	public static void assertSquareArrays(int xRes, int yRes, double[][]... arrs)
	{
		for (double[][] arr : arrs)
		{
			assertSquareArray(xRes, yRes, arr);
		}
	}

	public static void assertSquareArray(int xRes, int yRes, double[][] arr)
	{
		if (arr.length != xRes)
		{
			throw new RuntimeException("arr.length = " + arr.length + " expected length = " + xRes);
		}
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i].length != yRes)
			{
				throw new RuntimeException("arr[" + i + "].length = " + arr[i].length
						+ " expected length = " + yRes);
			}
		}
	}

}
