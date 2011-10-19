package ru.vasily.solver.factory;

import ru.vasily.solver.restorator.MinmodRestorator;
import ru.vasily.solver.restorator.NoOpRestorator;
import ru.vasily.solver.restorator.ThreePointRestorator;

public class RestoratorFactory
{
	public ThreePointRestorator createRestorator(String type)
	{
		if ("simple_minmod".equals(type))
		{
			return new MinmodRestorator();
		}
		else if ("no_op".equals(type))
		{
			return new NoOpRestorator();
		}
		else
		{
			throw new IllegalArgumentException("unsupported restorator type:" + type
					+ "only simple_minmod and no_op are supported ");
		}
	}
}
