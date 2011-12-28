package ru.vasily.solver;

import static ru.vasily.core.parallel.ParallelTaskUtils.getIndexOfPart;
import static ru.vasily.solverhelper.misc.ArrayUtils.isNAN;
import ru.vasily.core.parallel.ParallelEngine;
import ru.vasily.core.parallel.ParallelTask;
import ru.vasily.solver.restorator.ThreePointRestorator;
import ru.vasily.solver.riemann.RiemannSolver2D;
import ru.vasily.solver.utils.Restorator2dUtility;

public class RestoredFlowCalculator implements FlowCalculatorArray2D
{

	private final ThreePointRestorator rawRestorator;
	private final double gamma;
	private final RiemannSolver2D riemannSolver2D;
	private final ParallelEngine parallelEngine;

	public RestoredFlowCalculator(RiemannSolver2D riemannSolver2D, ThreePointRestorator restorator,
			ParallelEngine parallelEngine, double gamma)
	{
		this.riemannSolver2D = riemannSolver2D;
		this.rawRestorator = restorator;
		this.parallelEngine = parallelEngine;
		this.gamma = gamma;
	}

	private void setFlow(double[] flow, double[] uL, double[] uR, int i, int j,
			double cos_alfa, double sin_alfa)
	{
		riemannSolver2D.getFlow(flow, uL, uR, gamma, gamma, cos_alfa, sin_alfa);
		if (isNAN(flow))
		{
			throw AlgorithmError.builder()
					.put("error type", "NAN value in flow")
					.put("i", i).put("j", j)
					.put("left input", uL).put("right input", uR)
					.put("output", flow)
					.put("cos_alfa", cos_alfa).put("sin_alfa", sin_alfa)
					.build();
		}
	}

	private class FindCorrectorFlowTask implements ParallelTask
	{

		private final Restorator2dUtility restorator;
		private final double[][][] left_right_flow;
		private final double[][][] up_down_flow;
		private final int xRes;
		private final int yRes;

		public FindCorrectorFlowTask(Restorator2dUtility restorator, double[][][] left_right_flow,
				double[][][] up_down_flow, int xRes, int yRes)
		{
			this.restorator = restorator;
			this.left_right_flow = left_right_flow;
			this.up_down_flow = up_down_flow;
			this.xRes = xRes;
			this.yRes = yRes;
		}

		@Override
		public void doPart(double start, double end)
		{
			double[] uLeft_phy = new double[8];
			double[] uRight_phy = new double[8];
			double[] uUp_phy = new double[8];
			double[] uDown_phy = new double[8];

			int firstLoopStart = getIndexOfPart(1, xRes - 2, start);
			int firstLoopEnd = getIndexOfPart(1, xRes - 2, end);

			for (int i = firstLoopStart; i < firstLoopEnd; i++)
				for (int j = 2; j < yRes - 2; j++)
				{
					double[] flow = left_right_flow[i][j];
					restorator.restoreLeft(uLeft_phy, i, j);
					restorator.restoreRight(uRight_phy, i, j);
					setFlow(flow, uLeft_phy, uRight_phy, i, j, 1.0, 0.0);
				}

			int secondLoopStart = getIndexOfPart(2, xRes - 2, start);
			int secondLoopEnd = getIndexOfPart(2, xRes - 2, end);
			for (int i = secondLoopStart; i < secondLoopEnd; i++)
				for (int j = 1; j < yRes - 2; j++)
				{
					double[] flow = up_down_flow[i][j];
					restorator.restoreUp(uUp_phy, i, j);
					restorator.restoreDown(uDown_phy, i, j);
					setFlow(flow, uDown_phy, uUp_phy, i, j, 0.0, 1.0);
				}
		}
	}

	@Override
	public void calculateFlow(double[][][] left_right_flow, double[][][] up_down_flow, double[][][] consVals)
	{
		Restorator2dUtility restorator = new Restorator2dUtility(rawRestorator, consVals, gamma);
		int xRes = consVals.length;
		int yRes = consVals[0].length;
		parallelEngine.run(new FindCorrectorFlowTask(restorator, left_right_flow, up_down_flow,
				xRes, yRes));
	}
}
