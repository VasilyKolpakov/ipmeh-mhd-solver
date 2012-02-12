package ru.vasily.application.misc;

public class AreaMeter
{

    public static double area(
            double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double x4, double y4)
    {
        double v1x = x1 - x2;
        double v1y = y1 - y2;
        double v2x = x3 - x2;
        double v2y = y3 - y2;
        double v3x = x3 - x4;
        double v3y = y3 - y4;
        double v4x = x1 - x4;
        double v4y = y1 - y4;
        double firstArea = 0.5 * ((v1x * v2y) - v2x * v1y);
        double secondArea = 0.5 * ((v3x * v4y) - v4x * v3y);
        return Math.abs(firstArea) + Math.abs(secondArea);
    }
}
