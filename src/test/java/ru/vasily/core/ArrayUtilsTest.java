package ru.vasily.core;

import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilsTest
{
    @Test
    public void naive_cloning()
    {
        double arr[][] = {{1.0}, {2.0}};
        double[][] copy = arr.clone();
        arr[0][0] = 0;
        Assert.assertTrue(copy[0][0] == 0);
    }

    @Test
    public void copy()
    {
        double arr[][] = {{1.0}};
        double[][] copy = ArrayUtils.copy(arr);
        arr[0][0] = 0;
        Assert.assertTrue(copy[0][0] == 1.0);
    }

    @Test
    public void copy3d()
    {
        double arr[][][] = {{{1.0}}};
        double[][][] copy = ArrayUtils.copy(arr);
        arr[0][0][0] = 0;
        Assert.assertTrue(copy[0][0][0] == 1.0);
    }

    @Test
    public void copyTo3d()
    {
        double arr[][][] = {{{1.0}}};
        double[][][] copy = new double[1][1][1];
        ArrayUtils.copy(copy, arr);
        Assert.assertTrue(copy[0][0][0] == 1.0);
    }

    @Test
    public void copyTo2d()
    {
        double arr[][] = {{1.0}};
        double[][] copy = new double[1][1];
        ArrayUtils.copy(copy, arr);
        Assert.assertTrue(copy[0][0] == 1.0);
    }

    @Test
    public void copyTo1d()
    {
        double arr[] = {1.0};
        double[] copy = new double[1];
        ArrayUtils.copy(copy, arr);
        Assert.assertTrue(copy[0] == 1.0);
    }
}
