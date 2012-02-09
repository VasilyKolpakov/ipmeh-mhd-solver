package ru.vasily.solverhelper.misc;

import java.io.IOException;
import java.io.Reader;

import ru.vasily.core.Parser;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;

public class DataObjectParser implements Parser<DataObject>
{

    private final DataObjectService objectService;

    public DataObjectParser(DataObjectService objectService)
    {
        this.objectService = objectService;
    }

    @Override
    public DataObject parseFrom(Reader in) throws IOException
    {
        return objectService.readObject(in);
    }

}
