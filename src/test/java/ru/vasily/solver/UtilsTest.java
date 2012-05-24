package ru.vasily.solver;

import org.junit.Test;
import ru.vasily.solver.riemann.RiemannSolver;
import ru.vasily.solver.riemann.RoeSolverByKryukov;

import java.util.Arrays;

public class UtilsTest
{
    @Test
    public void steady_shock()
    {
        MHDValues leftValues = MHDValues.builder()
                                        .rho(0.2)
                                        .p(0.1)
                                        .u(1.0)
                                        .v(1.0)
                                        .w(0.0)
                                        .bX(1.0)
                                        .bY(0.0)
                                        .bZ(0.0)
                                        .build();
        double gamma = 1.6666;
        MHDValues rightValues = Utils.getSteadyShockRightValues(leftValues, gamma);
        double[] shockFlow = getFlow(leftValues, rightValues, gamma);
        double[] leftFlow = getFlow(leftValues, leftValues, gamma);
        double[] rightFlow = getFlow(rightValues, rightValues, gamma);
        System.out.println(Arrays.toString(shockFlow));
        System.out.println("left =  " + leftValues);
        System.out.println("right = " + rightValues);
        System.out.println("left  = " + Arrays.toString(leftFlow));
        System.out.println("right = " + Arrays.toString(rightFlow));
    }

    private double[] getFlow(MHDValues leftValues, MHDValues rightValues, double gamma)
    {
        double[] leftConsVals = toPhyVals(leftValues, gamma);
        double[] rightConsVals = toPhyVals(rightValues, gamma);
        RiemannSolver riemannSolver = new RoeSolverByKryukov();
        double[] flow = new double[8];
        riemannSolver.getFlow(flow, leftConsVals, rightConsVals, gamma, gamma);
        return flow;
    }

    private double[] toPhyVals(MHDValues leftValues, double gamma)
    {
        double[] leftConsVals = new double[8];
        leftValues.setToArray(leftConsVals, gamma);
        Utils.toPhysical(leftConsVals, leftConsVals, gamma);
        return leftConsVals;
    }

}
