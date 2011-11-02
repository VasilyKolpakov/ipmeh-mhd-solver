package ru.vasily.solverhelper;

import java.util.Map;
import java.util.concurrent.locks.Condition;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.solver.*;
import ru.vasily.solver.factory.IMHDSolverFactory;
import ru.vasily.solverhelper.misc.ISerializer;
import ru.vasily.solverhelper.plotdata.PlotData;
import ru.vasily.solverhelper.plotdata.PlotDataFactory;

public class MHDSolverFacade implements SolverFacade
{

	private final ISerializer serializer;
	private final IMHDSolverFactory solverFactory;

	public MHDSolverFacade(ISerializer serializer, IMHDSolverFactory solverFactory)
	{
		this.serializer = serializer;
		this.solverFactory = solverFactory;
	}

	@Override
	public CalculationResult solve(DataObject p)
	{
		MHDSolver solver = solverFactory.createSolver(p);
		return calculate(solver,
				iterateWithTimeLimit(solver, p.getObj("physicalConstants").getDouble("totalTime")));
	}

	private CalculationResult calculate(MHDSolver solver, Runnable calcTask)
	{
		try
		{
			calcTask.run();
		}
		catch (AlgorithmError err)
		{
			StringBuilder sb = new StringBuilder();
			serializer.writeObject(err.getParams(), sb);
			CalculationResult calculationResult = new CalculationResult(
					PlotDataFactory.emptyPlot(),
					sb.toString(), false
					);
			return calculationResult;
		}
		Map<String, Object> logData = solver.getLogData();
		CalculationResult calculationResult =
				createSuccessCalculationResult(solver.getData(), logData);
		return calculationResult;
	}

	@Override
	public IterativeSolver getSolver(final DataObject p)
	{
		return new IterativeSolver()
		{
			private MHDSolver solver = solverFactory.createSolver(p);

			@Override
			public CalculationResult next(int iterations)
			{
				return calculate(solver,
						iterateWithCountLimit(solver, iterations));
			}

		};
	}

	private CalculationResult createSuccessCalculationResult(
			PlotData plotData,
			Map<String, Object> logData)
	{
		StringBuilder sb = new StringBuilder();
		serializer.writeObject(logData, sb);
		CalculationResult calculationResult = new CalculationResult(
				plotData, "calculation done \n log = "
						+ sb.toString(), true);
		return calculationResult;
	}

	private static Runnable iterateWithCountAndTimeLimit(final MHDSolver solver,
			final int limit, final double totalTime)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; (i < limit) && (solver.getTotalTime() < totalTime); i++)
				{
					solver.nextTimeStep();
				}
			}
		};
	}

	private static Runnable iterateWithCountLimit(final MHDSolver solver,
			final int limit)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < limit; i++)
				{
					solver.nextTimeStep();
				}
			}
		};
	}

	private static Runnable iterateWithTimeLimit(final MHDSolver solver,
			final double totalTime)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				while (solver.getTotalTime() < totalTime)
				{
					solver.nextTimeStep();
				}
			}
		};
	}

	@Override
	public TimeLimitedIterativeSolver getTimeLimitedSolver(final DataObject p)
	{
		final double totalTime = p.getObj("physicalConstants").getDouble("totalTime");
		return new TimeLimitedIterativeSolver()
		{
			private MHDSolver solver = solverFactory.createSolver(p);
			private boolean currentResultSucsess = true;

			@Override
			public CalculationResult next(int iterations)
			{
				final CalculationResult currentResult = calculate(
						solver,
						iterateWithCountAndTimeLimit(solver, iterations,
								totalTime));
				currentResultSucsess = currentResult.sucsess;
				return currentResult;
			}

			@Override
			public boolean isTimeLimitReached()
			{
				return solver.getTotalTime() >= totalTime || (!currentResultSucsess);
			}

		};
	}
}