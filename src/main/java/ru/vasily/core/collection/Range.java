package ru.vasily.core.collection;

import java.util.Iterator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Range implements Iterable<Integer>
{
    private final int start;
    private final int end;
    private final int step;

    public static Iterable<Integer> range(int start, int end, int step)
    {
        return new Range(start, end, step);
    }

    public static Iterable<Integer> range(int start, int end)
    {
        return new Range(start, end, 1);
    }

    public static Iterable<Integer> range(int size)
    {
        return new Range(0, size, 1);
    }

    private Range(int start, int end, int step)
    {
        Preconditions.checkArgument(step != 0, "step == 0");
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    public Iterator<Integer> iterator()
    {
        return new RangeIterator(start, end, step);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper("Range").
                add("start", start).
                add("end", end).
                add("step", step).
                toString();
    }

    private static class RangeIterator implements Iterator<Integer>
    {
        private final int end;
        private int current;
        private final int step;

        public RangeIterator(int start, int end, int step)
        {
            this.end = end;
            current = start;
            this.step = step;
        }

        @Override
        public boolean hasNext()
        {
            return current < end;
        }

        @Override
        public Integer next()
        {
            int next = current;
            current += step;
            return next;
        }

        @Override
        public void remove()
        {
        }
    }
}
