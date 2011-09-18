package ru.vasily.solverhelper;

import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.*;

public class SolverFactory implements ISolverFactory
{

	@Override
	public MHDSolver createSolver(DataObject params)
	{
		String solverType = params.getString("solver");
		if (solverType.equalsIgnoreCase("1d"))
		{
			return new MHDSolver1D(params, new RoeSolverByKryukov());
		}
		else if (solverType.equalsIgnoreCase("2d"))
		{
			return new MHDSolver2D(params, new RiemannSolver1Dto2DWrapper(new RoeSolverByKryukov()));
		}
		throw new RuntimeException("incorrect solver type : " + solverType);
	}
}
