package ru.vasily.application.misc;

import java.io.IOException;
import java.io.Reader;

import ru.vasily.core.io.Parser;
import ru.vasily.core.dataobjs.DataObject;
import ru.vasily.core.dataobjs.DataObjectService;

public class DataObjectParser implements Parser<DataObject>
{

    private final DataObjectService objectService;

    public static Parser<DataObject> asDataObject(DataObjectService objectService)
    {
        return new DataObjectParser(objectService);
    }

    private DataObjectParser(DataObjectService objectService)
    {
        this.objectService = objectService;
    }

    @Override
    public DataObject parseFrom(Reader in) throws IOException
    {
        return objectService.readObject(in);
    }

}
