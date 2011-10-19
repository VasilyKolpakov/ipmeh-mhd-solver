package ru.vasily.core.parallel;

public interface IParallelEngine
{
	void run(ParallelTask task);

	void destroy();
}
