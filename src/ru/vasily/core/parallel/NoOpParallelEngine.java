package ru.vasily.core.parallel;

public class NoOpParallelEngine implements IParallelEngine
{
	public static final IParallelEngine INSTANCE = new NoOpParallelEngine();

	@Override
	public void run(ParallelTask task)
	{
		task.doPart(0, 1.0);
	}

	@Override
	public void destroy()
	{
	}
}
