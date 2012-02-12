package ru.vasily.test.matchers;

import com.google.common.io.CharStreams;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;


public class ReadableMatcher<T extends Readable> extends TypeSafeMatcher<T>
{
    private final String content;

    public static<T extends Readable> Matcher<T> readable(String content)
    {
        return new ReadableMatcher<T>(content);
    }

    private ReadableMatcher(String content)
    {
        this.content = content;
    }

    @Override
    public boolean matchesSafely(T readable)
    {
        try
        {
            return CharStreams.toString(readable).equals(readable);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("a Readable ").appendValue(content);
    }
}
