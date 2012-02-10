package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;

import ru.vasily.dataobjs.CalculationResult;

public interface IResultWriter
{
    void createResultDir(String directoryName, CalculationResult result)
            throws IOException;
}
