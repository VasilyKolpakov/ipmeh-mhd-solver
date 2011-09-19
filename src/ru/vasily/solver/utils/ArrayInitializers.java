package ru.vasily.solver.utils;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class ArrayInitializers
{
	public static Builder relative()
	{
		return new Builder();
	}

	public static class Builder
	{
		private List<ArrayInit> initializers = Lists.newArrayList();

		public Builder fill(ArrayInitFunction function)
		{
			square(function, 0, 0, 1, 1);
			return this;
		}

		public Builder square(double[] val, double x1, double y1, double x2, double y2)
		{
			square(new ConstantFunction(val), x1, y1, x2, y2);
			return this;
		}

		public Builder square(ArrayInitFunction function, double x1, double y1, double x2, double y2)
		{
			initializers.add(new SquareArrayInit(x1, y1, x2, y2, function));
			return this;
		}

		public void initialize(double[][][] vals)
		{
			for (ArrayInit arrayInit : initializers)
			{
				arrayInit.init(vals);
			}
		}
	}

	private static class SquareArrayInit implements ArrayInit
	{

		private final double x1;
		private final double y1;
		private final double x2;
		private final double y2;
		private final ArrayInitFunction function;

		public SquareArrayInit(double x1, double y1, double x2, double y2,
				ArrayInitFunction function)
		{
			this.function = function;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void init(double[][][] arr)
		{
			int xSize = arr.length;
			int ySize = arr[0].length;
			for (int i = (int) (xSize * x1); i < xSize * x2; i++)
			{
				for (int j = (int) (ySize * y1); j < ySize * y2; j++)
				{
					function.init(arr[i][j], (double) i / (xSize - 1), (double) j / (ySize - 1));
				}
			}

		}

	}

	private static class ConstantFunction implements ArrayInitFunction
	{
		private final double[] val;

		public ConstantFunction(double[] val)
		{
			this.val = val;
		}

		@Override
		public void init(double[] arr, double xRelative, double yRelative)
		{
			for (int i = 0; i < val.length; i++)
			{
				arr[i] = val[i];
			}
		}
	}

	public static void main(String[] args)
	{
		double[] val = { 1, 2, 3, 4 };
		double[][][] vals = new double[3][3][4];
		relative().square(val, 0.666667, 0, 1, 1).initialize(vals);
		for (double[][] ds : vals)
		{
			System.out.println(Arrays.deepToString(ds));
		}
		System.out.println((int) (0.6 * 3));
	}
}
