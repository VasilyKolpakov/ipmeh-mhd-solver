package ru.vasily.solver;

import static org.junit.Assert.*;

import static ru.vasily.solver.SolverMatchers.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import ru.vasily.solver.riemann.RiemannSolver;
import ru.vasily.solver.riemann.RoeSolverByKryukov;

public class RoeSolverByKryukovTest
{
    private RiemannSolver solver;

    @Before
    public void setup()
    {
        solver = new RoeSolverByKryukov();
    }

    @Test
    public void flow_direction()
    {
        double[] flow = new double[8];
        solver.getFlow(flow,
                       1, 0, 0, 0, 1, 0, 0, 0, 5.0 / 3.0,
                       10, 0, 0, 0, 10, 0, 0, 0, 5.0 / 3.0);
        Assert.assertTrue("flow directed left", flow[0] < 0);
    }

    @Test
    public void strange_phantom_flow()
    {
        double[] flow = new double[8];
        solver.getFlow(flow,
                       1, 0, 0, 0, 1, 0, 0, 0, 5.0 / 3.0,
                       1, 0, 0, 0, 1, 0, 0, 0, 5.0 / 3.0);
        assertThat(flow, tolerantlyEqualTo(
                new double[]{0.0,
                        1.0,
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
        ));
    }

    @Test
    public void perfomance()
    {
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

    private double rnd()
    {
        return Math.random();
    }
}
