package ru.vasily.solver.border;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionsBuilder
{

	private final ImmutableList.Builder<Array2dBorderConditions> builder;

	public ConditionsBuilder()
	{
		builder = ImmutableList.<Array2dBorderConditions>builder();
	}

	public ConditionsBuilder addConditions(Array2dBorderConditions conditions)
	{
		builder.add(conditions);
		return this;
	}

	public Array2dBorderConditions build()
	{
		return conditions(builder.build());
	}

	private static Array2dBorderConditions conditions(final List<Array2dBorderConditions> conditions)
	{
		return new Array2dBorderConditions()
		{
			@Override
			public void applyConditions(double[][][] values)
			{
				for (Array2dBorderConditions condition : conditions)
				{
					condition.applyConditions(values);
				}
			}
		};
	}
}
