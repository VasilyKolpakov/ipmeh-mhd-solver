package ru.vasily.core.parallel;

public class NoOpParallelEngine implements ParallelEngine
{
	public static final ParallelEngine INSTANCE = new NoOpParallelEngine();

	@Override
	public void run(SmartParallelTask task)
	{
	}
}
