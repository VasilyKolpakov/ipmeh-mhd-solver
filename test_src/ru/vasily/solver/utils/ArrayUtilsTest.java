package ru.vasily.solver.utils;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import ru.vasily.solverhelper.misc.ArrayUtils;

public class ArrayUtilsTest
{
	@Test
	public void naive_cloning()
	{
		double arr[][] = { { 1.0 }, { 2.0 } };
		double[][] copy = arr.clone();
		arr[0][0] = 0;
		Assert.assertTrue(copy[0][0] == 0);
	}

	@Test
	public void copy()
	{
		double arr[][] = { { 1.0 } };
		double[][] copy = ArrayUtils.copy(arr);
		arr[0][0] = 0;
		Assert.assertTrue(copy[0][0] == 1.0);
	}
}
