package ru.vasily.core.parallel;

import ru.vasily.core.collection.Reducer;

public interface ParallelManager
{
    Iterable<Integer> range(int start, int end, boolean sync);

    <E> E accumulate(Reducer<E, E> reducer, E result);

    boolean isMainThread();
}
