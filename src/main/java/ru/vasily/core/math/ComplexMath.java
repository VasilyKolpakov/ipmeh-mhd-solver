package ru.vasily.core.math;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import java.util.List;

import static java.lang.Math.*;
import static java.util.Arrays.asList;
import static ru.vasily.core.math.Complex.complex;
import static ru.vasily.core.math.Complex.complexRe;

public class ComplexMath
{
    /**
     * returns roots of ax^3 + bx^2 + cx + d = 0
     */
    public static List<Complex> roots(double a0, double a1, double a2, double a3)
    {
        double a = a3;
        double b = a2;
        double c = a1;
        double d = a0;
        double minus1_3a_div = -1.0 / (3 * a);
        double minusB_3a_div = -b / (3 * a);

        double q_0 = 2 * b * b * b - 9 * a * b * c + 27 * a * a * d;
        double q_1 = b * b - 3 * a * c;
        double Q = sqrt(q_0 * q_0 - 4 * q_1 * q_1 * q_1);
        double B = 2 * b * b * b - 9 * a * b * c + 27 * a * a * d;
        Complex A_plus = complexRe(cbrt(0.5 * (B + Q)));
        Complex A_minus = complexRe(cbrt(0.5 * (B - Q)));

        Complex x_1 = complexRe(minusB_3a_div)
                .add(
                        complexRe(minus1_3a_div)
                                .multiply(A_plus)
                )
                .add(
                        complexRe(minus1_3a_div)
                                .multiply(A_minus)
                );
        Complex x_2 = complexRe(minusB_3a_div)
                .add(
                        complex(1.0 / (6 * a), sqrt(3) / (6 * a))
                                .multiply(A_plus)
                )
                .add(
                        complex(1.0 / (6 * a), -sqrt(3) / (6 * a))
                                .multiply(A_minus)
                );
        Complex x_3 = complexRe(minusB_3a_div)
                .add(
                        complex(1.0 / (6 * a), -sqrt(3) / (6 * a))
                                .multiply(A_plus)
                )
                .add(
                        complex(1.0 / (6 * a), sqrt(3) / (6 * a))
                                .multiply(A_minus)
                );

        return asList(x_1, x_2, x_3);
    }

    public static Complex polynomial(Complex c, double... coefficients)
    {
        Complex result = complex(0, 0);
        List<Double> coefficientsList = Doubles.asList(coefficients);
        Complex powerOfC = complexRe(1);
        for (double coefficient : coefficientsList)
        {
            Complex polynomialPart = powerOfC.multiply(complexRe(coefficient));
            result = result.add(polynomialPart);
            powerOfC = powerOfC.multiply(c);
        }
        return result;
    }

}
