package ru.vasily.solver.restorator;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.junit.Assert.assertThat;
import static ru.vasily.core.collection.Range.range;
import static ru.vasily.test.matchers.DoubleInIntervalMatcher.between;

public class EtaOmegaRestoratorTest
{
    private EtaOmegaRestorator restorator = new EtaOmegaRestorator(0.0, 1.0);

    @Test
    public void linear()
    {
        test(0.5, 0, 1, 2);
    }

    @Test
    public void non_linear_right()
    {
        test(1.5, 1, 2, 100);
    }

    @Test
    public void local_min_left()
    {
        test(1, 2, 1, 2);
    }

    private void test(double expected, double vLeft, double vRight, double vRightRight)
    {
        double restored = restorator.restore(vLeft, vRight, vRightRight);
        Assert.assertEquals(expected, restored, 0.0000001);
    }

    @Test
    public void monotony_simple()
    {
        assertMonotony(2.1, 2, 1);
    }

    @Test
    public void monotony_rnd()
    {
        Random rnd = new Random(0);
        for (int i : range(1000))
        {
            double vLeft = rnd.nextDouble();
            double vRight = rnd.nextDouble();
            double vRightRight = rnd.nextDouble();
            assertMonotony(vLeft, vRight, vRightRight);
        }
    }

    private void assertMonotony(double vLeft, double vRight, double vRightRight)
    {
        double restored = new EtaRestorator(1.0 / 3).restore(vLeft, vRight, vRightRight);
        String reason = String.format("monotony for vLeft = %s, vRight = %s, vRightRight = %s",
                vLeft, vRight, vRightRight);
        assertThat(reason, restored, between(vLeft, vRight));
    }

}
