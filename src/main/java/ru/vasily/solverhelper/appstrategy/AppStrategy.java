package ru.vasily.solverhelper.appstrategy;

import java.io.File;
import java.io.IOException;

public interface AppStrategy
{
    void processInputFile(String inputFile) throws IOException;
}
