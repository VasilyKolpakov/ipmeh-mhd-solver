package ru.vasily.core.math;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JavaMathTest
{
    @Test
    public void int_to_double_conversion()
    {
        assertThat(2.0 / 1, is(2.0));
        assertThat(2 / 1.0, is(2.0));
        assertThat(2 * 1.0, is(2.0));
        assertThat(2.0 * 1, is(2.0));

        assertThat(2 * 10, is(20));
        assertThat(2 / 10, is(0));
    }
}
