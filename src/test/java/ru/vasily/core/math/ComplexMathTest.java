package ru.vasily.core.math;

import org.hamcrest.*;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static ru.vasily.core.math.Complex.*;
import static ru.vasily.core.math.ComplexMath.*;

public class ComplexMathTest
{
    private static final double EPSILON = 0.00000000000001;

    @Test
    public void cubic_roots()
    {
        checkRoots(1, 1, 1, 1);
        checkRoots(1, 2, 3, 4);
        checkRoots(13, 1, 12, 1);
        checkRoots(1, 12, 1, 11);
    }

    @Test
    public void simple_poly()
    {
        List<Complex> roots = roots(0, 1, -2, 1);
        assertThat(roots, hasItem(complexNumber(0, 0)));
        assertThat(roots, hasItem(complexNumber(1, 0)));
        assertThat(roots, hasItem(complexNumber(1, 0)));
    }

    private void checkRoots(double a, double b, double c, double d)
    {
        List<Complex> roots = roots(a, b, c, d);
        assertThat(roots.size(), is(3));
        for (Complex root : roots)
        {
            Complex polyValue = polynomial(root, a, b, c, d);
            assertThat(polyValue.modulusSquare(), is(closeTo(0, 0.00000001)));
        }
    }

    @Test
    public void simple_polynomial()
    {
        Complex c = polynomial(complexRe(3), 0, 0, 1);
        assertThat(c, is(complexRe(9)));
    }

    @Test
    public void another_polynomial()
    {
        Complex c = polynomial(complexRe(3), 7, 0, 0);
        assertThat(c, is(complexRe(7)));
    }

    public static Matcher<Complex> complexNumber(Matcher<Double> reMatcher, Matcher<Double> imMatcher)
    {
        return new ComplexMatcher(reMatcher, imMatcher);
    }

    public static Matcher<Complex> complexNumber(double re, double im)
    {
        return complexNumber(closeTo(re, EPSILON), closeTo(im, EPSILON));
    }

    private static class ComplexMatcher extends TypeSafeMatcher<Complex>
    {

        private final Matcher<Double> reMatcher;
        private final Matcher<Double> imMatcher;

        private ComplexMatcher(Matcher<Double> reMatcher, Matcher<Double> imMatcher)
        {
            this.reMatcher = reMatcher;
            this.imMatcher = imMatcher;
        }

        @Override
        public boolean matchesSafely(Complex complex)
        {
            return reMatcher.matches(complex.re) && imMatcher.matches(complex.im);
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("a Complex number having ");
            reMatcher.describeTo(description);
            description.appendText(" as real part and ");
            imMatcher.describeTo(description);
            description.appendText(" as imaginary part");
        }
    }
}
