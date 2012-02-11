package ru.vasily.solverhelper.misc;

import com.google.common.base.Throwables;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import ru.vasily.core.Writable;

import java.io.IOException;

public class Serializer implements ISerializer
{
    private final ObjectReader reader;
    private final ObjectWriter writer;

    public Serializer()
    {
        super();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ALLOW_COMMENTS, true);
        this.reader = mapper.reader();
        this.writer = mapper.writer(new DefaultPrettyPrinter());
    }

    @Override
    public Writable asWritable(final Object obj)
    {
        return new Writable()
        {
            @Override
            public void writeTo(Appendable target) throws IOException
            {
                try
                {
                    String out = writer.writeValueAsString(obj);
                    target.append(out);
                }
                catch (Exception e)
                {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

}
