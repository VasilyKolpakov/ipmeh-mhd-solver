package ru.vasily.core.math;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static ru.vasily.core.math.Complex.complex;

public class ComplexTest
{
    @Test
    public void addition_re()
    {
        Complex c = complex(1, 0).add(complex(2, 0));
        assertThat(c, is(complex(3, 0)));
    }

    @Test
    public void addition_im()
    {
        Complex c = complex(0, 1).add(complex(0, 2));
        assertThat(c, is(complex(0, 3)));
    }

    @Test
    public void subtraction_re()
    {
        Complex c = complex(1, 0).subtract(complex(2, 0));
        assertThat(c, is(complex(-1, 0)));
    }

    @Test
    public void subtraction_im()
    {
        Complex c = complex(0, 1).subtract(complex(0, 2));
        assertThat(c, is(complex(0, -1)));
    }

    @Test
    public void division_re()
    {
        Complex c = complex(10, 0).divide(complex(2, 0));
        assertThat(c, is(complex(5, 0)));
    }

    @Test
    public void division_im()
    {
        Complex c = complex(0, 10).divide(complex(0, 2));
        assertThat(c, is(complex(5, 0)));
    }

    @Test
    public void division_complex()
    {
        Complex c = complex(2, 1).divide(complex(3, 2)).multiply(complex(3, 2));
        assertThat(c, is(complex(2, 1)));
    }

}
