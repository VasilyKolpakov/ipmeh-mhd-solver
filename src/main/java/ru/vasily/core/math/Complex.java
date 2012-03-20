package ru.vasily.core.math;

import static java.lang.Math.sqrt;

public class Complex
{
    public final double re;
    public final double im;

    public static Complex complex(double re, double im)
    {
        return new Complex(re, im);
    }

    public static Complex complexRe(double re)
    {
        return new Complex(re, 0);
    }

    public static Complex complexIm(double im)
    {
        return new Complex(0, im);
    }

    public Complex(double re, double im)
    {
        this.re = re;
        this.im = im;
    }

    public Complex multiply(Complex c)
    {
        return complex(re * c.re - im * c.im, re * c.im + im * c.re);
    }

    public Complex add(Complex c)
    {
        return complex(re + c.re, im + c.im);
    }

    public Complex subtract(Complex c)
    {
        return complex(re - c.re, im - c.im);
    }

    public double modulus()
    {
        return sqrt(modulusSquare());
    }

    public double modulusSquare()
    {
        return re * re + im * im;
    }

    public Complex conjugate()
    {
        return complex(re, -im);
    }

    public Complex divide(Complex c)
    {
        Complex invertedModulusSquare = complex(1 / c.modulusSquare(), 0);
        return this.multiply(c.conjugate()).multiply(invertedModulusSquare);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Complex complex = (Complex) o;

        if (Double.compare(complex.im, im) != 0)
        {
            return false;
        }
        if (Double.compare(complex.re, re) != 0)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = re != +0.0d ? Double.doubleToLongBits(re) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = im != +0.0d ? Double.doubleToLongBits(im) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
