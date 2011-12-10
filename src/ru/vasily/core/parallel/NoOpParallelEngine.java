package ru.vasily.core.parallel;

public class NoOpParallelEngine implements ParallelEngine
{
	public static final ParallelEngine INSTANCE = new NoOpParallelEngine();

	@Override
	public void run(ParallelTask task)
	{
		task.doPart(0, 1.0);
	}

	@Override
	public void run(SmartParallelTask task)
	{
	}
}
