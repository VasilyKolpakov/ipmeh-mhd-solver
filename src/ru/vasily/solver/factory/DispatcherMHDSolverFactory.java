package ru.vasily.solver.factory;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.MHDSolver;

public class DispatcherMHDSolverFactory implements IMHDSolverFactory
{
	private final Map<String, IMHDSolverFactory> solverFactories;

	public DispatcherMHDSolverFactory(MHDSolver1DFactory solver1dFactory,
			MHDSolver2DFactory solver2dFactory)
	{
		solverFactories = ImmutableMap
				.<String, IMHDSolverFactory> builder().
				put("1d", solver1dFactory).
				put("2d", solver2dFactory).
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
