package ru.vasily.core.templates;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.io.CharStreams.asWriter;

public class VelocityTemplateEngine
{
    public void evaluate(Map<String, ?> context, Appendable output, Reader templateReader)
    {
        Map<String, ?> mutableContext = new HashMap<String, Object>(context);
        VelocityContext velocityContext = new VelocityContext(mutableContext);
        try
        {
            Velocity.evaluate(velocityContext, asWriter(output), "LOG", templateReader);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
