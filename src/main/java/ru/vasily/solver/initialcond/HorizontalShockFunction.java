package ru.vasily.solver.initialcond;

import ru.vasily.solver.MHDValues;

import static ru.vasily.solver.Utils.fastShockSpeed;
import static ru.vasily.solver.Utils.setConservativeValues;

public class HorizontalShockFunction implements Init2dFunction
{
    public static final double FLAT_ANGLE_IN_DEGREES = 180.0;
    private final MHDValues left;
    private final MHDValues right;
    private final double x_s;
    private final double gamma;

    /**
     * @param left
     * @param right
     * @param x_s
     * @param gamma
     */
    public HorizontalShockFunction(MHDValues left,
                                   MHDValues right,
                                   double x_s,
                                   double gamma)
    {
        this.left = left;
        this.right = right;
        this.x_s = x_s;
        this.gamma = gamma;
        System.out.println("InitialConditionsFactories$SteadyShock.createFunction");
        System.out.printf("left = %s, right = %s\n", left, right);
        double fastShockSpeedLeft = fastShockSpeed(left, left.bX, gamma);
        double fastShockSpeedRight = fastShockSpeed(right, right.bX, gamma);
        System.out.printf("fss_left = %s, fss_right = %s\n", fastShockSpeedLeft, fastShockSpeedRight);

    }

    @Override
    public void apply(double[] arr, double x, double y)
    {
        if (x < x_s)
        {
            setConservativeValues(left, arr, gamma);
        }
        else
        {
            setConservativeValues(right, arr, gamma);
        }
    }
}
