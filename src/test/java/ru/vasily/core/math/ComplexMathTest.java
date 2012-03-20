package ru.vasily.core.math;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static ru.vasily.core.math.Complex.*;
import static ru.vasily.core.math.ComplexMath.*;

public class ComplexMathTest
{
    @Test
    public void cubic_roots()
    {
        checkRoots(1,1,1,1);
        checkRoots(1,2,3,4);
        checkRoots(13,1,12,1);
        checkRoots(1,12,1,11);
    }

    private void checkRoots(double a, double b, double c, double d)
    {
        List<Complex> roots = roots(a, b, c, d);
        assertThat(roots.size(), is(3));
        for (Complex root : roots)
        {
            Complex polyValue = polynomial(root, a, b, c, d);
            assertThat(polyValue.modulusSquare(), is(closeTo(0, 0.00001)));
        }
    }

    @Test
    public void simple_polynomial()
    {
        Complex c = polynomial(complexRe(3), 1, 0, 0);
        assertThat(c, is(complexRe(9)));
    }

    @Test
    public void another_polynomial()
    {
        Complex c = polynomial(complexRe(3), 0, 0, 7);
        assertThat(c, is(complexRe(7)));
    }
}
