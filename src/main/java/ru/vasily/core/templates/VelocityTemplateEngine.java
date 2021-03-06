package ru.vasily.core.templates;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import ru.vasily.core.io.Writable;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.io.CharStreams.asWriter;

public class VelocityTemplateEngine implements TemplateEngine
{
    @Override
    public Template createTemplate(String templateSource)
    {
        return new VelocityTemplate(templateSource);
    }

    private static class VelocityTemplate implements Template
    {
        private final String templateCode;

        private VelocityTemplate(String templateCode)
        {
            this.templateCode = templateCode;
        }

        @Override
        public Writable evaluate(final Map<String, ?> context)
        {
            return new WritableTemplateOutput(templateCode, context);
        }
    }

    private static class WritableTemplateOutput implements Writable
    {
        private final String templateCode;
        private final Map<String, ?> context;

        private WritableTemplateOutput(String templateCode, Map<String, ?> context)
        {
            this.templateCode = templateCode;
            this.context = context;
        }

        @Override
        public void writeTo(Appendable output) throws IOException
        {
            Map<String, ?> mutableContext = new HashMap<String, Object>(context);
            Context velocityContext = new VelocityContext(mutableContext);
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
