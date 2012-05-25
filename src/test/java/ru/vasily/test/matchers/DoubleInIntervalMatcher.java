package ru.vasily.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.junit.Assert.assertThat;

public class DoubleInIntervalMatcher extends TypeSafeMatcher<Double>
{
    private final double a;
    private final double b;

    public DoubleInIntervalMatcher(double a, double b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean matchesSafely(Double x)
    {
        double d1 = a - x;
        double d2 = b - x;
        return d1 * d2 <= 0;
    }

    @Override
    public void describeTo(Description description)
    {
        description
                .appendText("a double between ")
                .appendValue(a)
                .appendText(" and ")
                .appendValue(b);
    }

    public static Matcher<Double> between(double a, double b)
    {
        return new DoubleInIntervalMatcher(a, b);
    }
}
