package ru.vasily.core.math;

public class Vector2
{
    public final double x1;
    public final double x2;

    public Vector2(double x1, double x2)
    {
        this.x1 = x1;
        this.x2 = x2;
    }

    public Vector2 add(Vector2 vector)
    {
        return new Vector2(x1 + vector.x1, x2 + vector.x2);
    }

    public Vector2 multiply(double number)
    {
        return new Vector2(x1 * number, x2 * number);
    }

    public double dot(Vector2 vector)
    {
        return x1 * vector.x1 + x2 * vector.x2;
    }

    public double square()
    {
        return dot(this);
    }

    @Override
    public String toString()
    {
        return "Vector2{" +
                "x1=" + x1 +
                ", x2=" + x2 +
                '}';
    }

}

