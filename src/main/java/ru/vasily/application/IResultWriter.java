package ru.vasily.application;

import java.io.IOException;

import ru.vasily.solver.CalculationResult;

public interface IResultWriter
{
    void createResultDir(String directoryName, CalculationResult result)
            throws IOException;
}
