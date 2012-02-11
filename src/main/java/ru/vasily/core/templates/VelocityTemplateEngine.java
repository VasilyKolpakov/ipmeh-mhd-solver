package ru.vasily.core.templates;

import com.google.common.io.CharStreams;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.io.CharStreams.asWriter;

public class VelocityTemplateEngine implements TemplateEngine
{
    @Override
    public Template createTemplate(Reader templateReader)
    {
        try
        {
            return new VelocityTemplate(CharStreams.toString(templateReader));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class VelocityTemplate implements Template
    {
        private final String templateCode;

        private VelocityTemplate(String templateCode)
        {
            this.templateCode = templateCode;
        }

        @Override
        public void evaluate(Map<String, ?> context, Appendable output)
        {
            Map<String, ?> mutableContext = new HashMap<String, Object>(context);
            VelocityContext velocityContext = new VelocityContext(mutableContext);
            try
            {
                Velocity.evaluate(velocityContext, asWriter(output), "LOG", new StringReader(templateCode));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
