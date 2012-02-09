package ru.vasily.dataobjs;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class JacksonDataObjServiceTest
{
    @Test
    public void list_parsing() throws IOException
    {
        DataObject data = new JacksonDataObjService().readObject(new StringReader(
                "{ \"list\" :" +
                        "[{\"name\":\"value\"},{\"name2\":\"value2\"}]}"));
        List<DataObject> list = data.getObjects("list");
        assertThat(list.get(0).getString("name"), is("value"));
        assertThat(list.get(1).getString("name2"), is("value2"));
    }
}
