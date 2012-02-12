package ru.vasily.core.templates;

import org.junit.Before;
import org.junit.Test;

import static ru.vasily.core.collection.Range.*;

import java.io.IOException;

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
        Iterable<Integer> range = range(1, 4);
        String template = "#foreach ($item in $range)$item #end";
        StringBuilder stringBuilder = new StringBuilder();
        templateEngine.createTemplate(template)
                .evaluate(singletonMap("range", range)).writeTo(stringBuilder);
        assertThat(stringBuilder.toString(), is("1 2 3 "));
    }
}
