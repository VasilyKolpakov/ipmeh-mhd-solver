package ru.vasily.core.parallel;

public interface ParallelManager
{
	void sync();

	Iterable<Integer> range(int start, int end);
}
