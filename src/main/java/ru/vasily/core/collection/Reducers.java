package ru.vasily.core.collection;

import static java.lang.Math.*;

import java.util.Iterator;

import static com.google.common.base.Preconditions.*;

public final class Reducers
{
    private static final Reducer<Double, Double> MINIMUM = new Reducer<Double, Double>()
    {
        @Override
        public Double add(Double container, Double element)
        {
            return min(container, element);
        }
    };

    private static final Reducer<Long, Long> LONG_SUM = new Reducer<Long, Long>()
    {
        @Override
        public Long add(Long container, Long element)
        {
            return container + element;
        }
    };

    private Reducers()
    {
    }

    public static Reducer<Double, Double> minimum()
    {
        return MINIMUM;
    }

    public static Reducer<Long, Long> longSum()
    {
        return LONG_SUM;
    }

    public static <R, E> R reduce(Reducer<R, E> reducer, Iterable<E> elements, R initialValue)
    {
        R accumulator = initialValue;
        for (E e : elements)
        {
            accumulator = reducer.add(accumulator, e);
        }
        return accumulator;
    }

    public static <E> E reduce(Reducer<E, E> reducer, Iterable<E> elements)
    {
        Iterator<E> elementsIterator = elements.iterator();
        checkArgument(elementsIterator.hasNext(), "empty iterable");
        E accumulator = elementsIterator.next();
        while (elementsIterator.hasNext())
        {
            accumulator = reducer.add(accumulator, elementsIterator.next());
        }
        return accumulator;
    }

}
