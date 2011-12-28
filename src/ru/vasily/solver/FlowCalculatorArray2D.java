package ru.vasily.solver;

import ru.vasily.core.parallel.ParallelManager;

public interface FlowCalculatorArray2D
{
	void calculateFlow(ParallelManager par, double[][][] left_right_flow, double[][][] up_down_flow, double[][][] consVals);
}
