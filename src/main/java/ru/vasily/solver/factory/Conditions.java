package ru.vasily.solver.factory;

import ru.vasily.solver.border.Array2dBorderConditions;

class Conditions
{
    public final Array2dBorderConditions borderConditions;
    public final double[][][] initialConditions;

    Conditions(Array2dBorderConditions borderConditions, double[][][] initialConditions)
    {
        this.borderConditions = borderConditions;
        this.initialConditions = initialConditions;
    }
}
