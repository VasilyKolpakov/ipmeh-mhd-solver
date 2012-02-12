package ru.vasily.core;

import org.junit.Before;
import org.junit.Test;
import ru.vasily.core.io.RealFileSystem;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RealFileSystemTest
{

    private RealFileSystem fileSystem;

    @Before
    public void setUp() throws Exception
    {
        fileSystem = new RealFileSystem();
    }

    @Test
    public void create_path() throws Exception
    {
        String pathSeparator = System.getProperty("file.separator");
        String actual = fileSystem.createPath("parent", "child1", "child2");
        String expected = "parent" + pathSeparator + "child1" + pathSeparator + "child2";
        assertThat(actual, is(expected));
    }

}
