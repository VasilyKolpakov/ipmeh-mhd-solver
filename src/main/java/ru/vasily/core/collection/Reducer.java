package ru.vasily.core.collection;

public interface Reducer<R, E>
{
    R add(R accumulator, E element);
}
