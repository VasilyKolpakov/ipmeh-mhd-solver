package ru.vasily.core.dataobjs;

import java.io.IOException;
import java.io.Reader;

public interface DataObjectService
{
    DataObject readObject(Reader source) throws IOException;
}
