package ru.vasily.core.templates;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VelocityTemplateEngineTest
{
    private VelocityTemplateEngine templateEngine;

    @Before
    public void setup()
    {
        templateEngine = new VelocityTemplateEngine();
    }

    @Test
    public void example() throws IOException
    {
        List<String> list = asList("1", "2", "3");
        String template = "#foreach ($item in $list)$item #end";
        StringBuilder stringBuilder = new StringBuilder();
        templateEngine.createTemplate(new StringReader(template))
                .evaluate(singletonMap("list", list)).writeTo(stringBuilder);
        assertThat(stringBuilder.toString(), is("1 2 3 "));
    }
}
