package ru.vasily.solver;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class RoeSolverByKryukovTest {
	private RiemannSolver solver;

	@Before
	public void setup() {
		solver = new RoeSolverByKryukov();
	}

	@Test
	public void flow_direction() {
		double[] flow = new double[8];
		solver.getFlow(flow,
				1, 0, 0, 0, 1, 0, 0, 0, 5.0 / 3.0,
				10, 0, 0, 0, 10, 0, 0, 0, 5.0 / 3.0);
		Assert.assertTrue("flow directed left", flow[0] < 0);
	}

	@Test
	public void perfomance() {
		double[] flow = new double[8];
		long time = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++)
		{
			solver.getFlow(flow,
					rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), 5.0 / 3.0,
					rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), rnd(), 5.0 / 3.0);
		}
		System.out.println((System.currentTimeMillis() - time));
	}

	private double rnd() {
		return Math.random();
	}
}
