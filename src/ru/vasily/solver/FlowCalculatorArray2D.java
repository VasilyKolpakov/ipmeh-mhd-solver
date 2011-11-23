package ru.vasily.solver;

public interface FlowCalculatorArray2D
{
	void calculateFlow(double[][][] left_right_flow, double[][][] up_down_flow, double[][][] consVals);
}
