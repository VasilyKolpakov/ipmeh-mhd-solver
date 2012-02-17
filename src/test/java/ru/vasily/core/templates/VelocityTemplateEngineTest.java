package ru.vasily.core.templates;

import org.junit.Before;
import org.junit.Test;
import ru.vasily.core.io.Writable;
import ru.vasily.core.io.Writables;

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
        Writable output = templateEngine.createTemplate(template).evaluate(singletonMap("range", range));
        assertThat(Writables.toString(output), is("1 2 3 "));
    }
}
