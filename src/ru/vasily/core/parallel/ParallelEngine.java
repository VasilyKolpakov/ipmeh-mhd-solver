package ru.vasily.core.parallel;

public interface ParallelEngine
{
	void run(ParallelTask task);

	void run(SmartParallelTask task);
}
