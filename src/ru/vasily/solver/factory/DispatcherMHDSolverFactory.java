package ru.vasily.solver.factory;

import java.util.Map;

import javax.management.RuntimeErrorException;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;

public class DispatcherMHDSolverFactory implements IMHDSolverFactory
{
	private final Map<String, IMHDSolverFactory> solverFactories;

	public DispatcherMHDSolverFactory(RestoratorFactory restoratorFactory)
	{
		solverFactories = ImmutableMap
				.<String, IMHDSolverFactory> builder().
				put("1d", new MHDSolver1DFactory(restoratorFactory)).
				put("2d", new MHDSolver2DFactory(restoratorFactory)).
				build();
	}

	@Override
	public MHDSolver createSolver(DataObject params)
	{
		String solverType = params.getString("solver");
		IMHDSolverFactory solverFactory = solverFactories.get(solverType.toLowerCase());
		if (solverFactory == null)
		{
			throw new RuntimeException("incorrect solver type : " + solverType);
		}
		return solverFactory.createSolver(params);
	}

}
