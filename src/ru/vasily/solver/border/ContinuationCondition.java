package ru.vasily.solver.border;

import static ru.vasily.solverhelper.misc.ArrayUtils.copy;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 10:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContinuationCondition implements Array2dBorderConditions
{
	private final Location l;
	private final int xRes;
	private final int yRes;

	public enum Location
	{
		TOP, BOTTOM, LEFT, RIGHT, All
	}

	public ContinuationCondition(Location l, int xRes, int yRes)
	{
		this.l = l;
		this.xRes = xRes;
		this.yRes = yRes;
	}

	@Override
	public void applyConditions(double[][][] data)
	{
		switch (l)
		{
			case TOP:
				for (int i = 0; i < xRes; i++)
				{
					copy(data[i][yRes - 2], data[i][yRes - 3]);
					copy(data[i][yRes - 1], data[i][yRes - 2]);
				}
				break;
			case BOTTOM:
				for (int i = 0; i < xRes; i++)
				{
					copy(data[i][1], data[i][2]);
					copy(data[i][0], data[i][1]);
				}
				break;
			case LEFT:
				for (int j = 0; j < yRes; j++)
				{
					copy(data[0][j], data[1][j]);
					copy(data[1][j], data[2][j]);
				}
				break;
			case RIGHT:
				for (int j = 0; j < yRes; j++)
				{
					copy(data[xRes - 2][j], data[xRes - 3][j]);
					copy(data[xRes - 1][j], data[xRes - 2][j]);
				}
				break;
			case All:
				for (int i = 0; i < xRes; i++)
				{
					copy(data[i][yRes - 2], data[i][yRes - 3]);
					copy(data[i][yRes - 1], data[i][yRes - 2]);
					copy(data[i][1], data[i][2]);
					copy(data[i][0], data[i][1]);
				}
				for (int j = 0; j < yRes; j++)
				{
					copy(data[0][j], data[1][j]);
					copy(data[1][j], data[2][j]);
					copy(data[xRes - 2][j], data[xRes - 3][j]);
					copy(data[xRes - 1][j], data[xRes - 2][j]);
				}
				break;
		}
	}
}
