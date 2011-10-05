package ru.vasily.solver.initialcond;

import ru.vasily.solverhelper.misc.ArrayUtils;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Array2dFiller implements InitialValues2dBuilder<double[][][]>
{
	private final int xRes;
	private final int yRes;
	private final double xSize;
	private final double ySize;
	private final double[][][] array;

	public Array2dFiller(int xRes, int yRes, double xSize, double ySize)
	{
		this.xRes = xRes;
		this.yRes = yRes;
		this.xSize = xSize;
		this.ySize = ySize;
		array = new double[xRes][yRes][8];
	}

	@Override
	public void apply(Init2dFunction function)
	{
		for (int i = 0; i < xRes; i++)
		{
			for (int j = 0; j < yRes; j++)
			{
				double x = xSize / (xRes - 1) * i;
				double y = ySize / (yRes - 1) * j;
				function.apply(array[i][j], x, y);
			}
		}
	}

	@Override
	public double[][][] build()
	{
		return ArrayUtils.copy(array);
	}
}
