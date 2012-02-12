package ru.vasily.application.appstrategy;

import java.io.IOException;

public interface AppStrategy
{
    void processInputFile(String inputFile) throws IOException;
}
