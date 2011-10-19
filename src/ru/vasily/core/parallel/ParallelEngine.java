package ru.vasily.core.parallel;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ParallelEngine implements IParallelEngine
{
	private final int numberOfAdditionalThreads;
	private final ExecutorService executor;

	public ParallelEngine()
	{
		numberOfAdditionalThreads = 2;
		executor = Executors.newCachedThreadPool();
	}

	@Override
	public void run(ParallelTask task)
	{
		int numberOfThreads = numberOfAdditionalThreads + 1;
		double fraction = 1.0 / numberOfThreads;
		Preconditions.checkState(fraction * numberOfThreads == 1.0,
				"rounding problem: fraction * numberOfThreads != 1.0, numberOfThreads = %s",
				numberOfThreads);
		List<Future<?>> futures = Lists.newArrayListWithCapacity(numberOfAdditionalThreads);
		for (int i = 1; i < numberOfThreads; i++)
		{
			Future<?> future = executor.submit(new ParallelTaskRunnable(fraction * i, fraction
					* (i + 1), task));
			futures.add(future);
		}
		task.doPart(0, fraction);
		waitForOtherThreads(futures);
	}

	private void waitForOtherThreads(List<Future<?>> futures)
	{
		try
		{
			for (Future<?> future : futures)
			{
				future.get();
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		catch (ExecutionException e)
		{
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException)
			{
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		}
	}

	private static class ParallelTaskRunnable implements Runnable
	{
		private final double start;
		private final double end;
		private final ParallelTask task;

		public ParallelTaskRunnable(double start, double end, ParallelTask task)
		{
			this.start = start;
			this.end = end;
			this.task = task;
		}

		@Override
		public void run()
		{
			task.doPart(start, end);
		}
	}

	@Override
	public void destroy()
	{
		executor.shutdownNow();
	}
}
