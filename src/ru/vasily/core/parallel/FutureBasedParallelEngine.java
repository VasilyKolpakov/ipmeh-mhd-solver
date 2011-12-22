package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import ru.vasily.core.collection.Range;
import ru.vasily.core.collection.Reducer;
import static ru.vasily.core.collection.Reducers.*;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class FutureBasedParallelEngine implements ParallelEngine
{
	private final int numberOfAdditionalThreads;
	private final ExecutorService executor;
	private final double fraction;

	public FutureBasedParallelEngine(int numberOfThreads)
	{
		checkArgument(numberOfThreads > 0, "number of threads must be > 0");
		this.numberOfAdditionalThreads = numberOfThreads - 1;
		fraction = 1.0 / numberOfThreads;
		checkState(fraction * numberOfThreads == 1.0,
				"rounding problem: fraction * numberOfThreads != 1.0, numberOfThreads = %s",
				numberOfThreads);
		executor = Executors.newCachedThreadPool(new ThreadFactory()
		{

			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
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
		catch (ExecutionException e)
		{
			Throwable cause = e.getCause();
			throw Throwables.propagate(cause);
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
	}

	private static class ParallelTaskRunnable implements Runnable
	{
		private final SmartParallelTask task;
		private final ParallelManager par;

		public ParallelTaskRunnable(ParallelManager par, SmartParallelTask task)
		{
			this.par = par;
			this.task = task;
		}

		@Override
		public void run()
		{
			task.doTask(par);
		}
	}

	@Override
	public void run(SmartParallelTask task)
	{
		CyclicBarrier barrier = new CyclicBarrier(numberOfAdditionalThreads + 1);
		AtomicReference<List<Object>> aggregationList = new AtomicReference<List<Object>>();
		int numberOfThreads = numberOfAdditionalThreads + 1;
		List<Future<?>> futures = Lists.newArrayListWithCapacity(numberOfAdditionalThreads);
		for (int i = 1; i < numberOfThreads; i++)
		{
			ParallelManager par = new ParallelManagerImpl(fraction * i, fraction * (i + 1),
					barrier, aggregationList);
			ParallelTaskRunnable runnableTask = new ParallelTaskRunnable(par, task);
			Future<?> future = executor.submit(runnableTask);
			futures.add(future);
		}
		task.doTask(new ParallelManagerImpl(0, fraction, barrier, aggregationList, true));
		waitForOtherThreads(futures);
	}

	private static class ParallelManagerImpl implements ParallelManager
	{

		private final double start;
		private final double end;
		private final CyclicBarrier barrier;
		private final AtomicReference<List<Object>> aggregationList;
		private final boolean isMainThread;

		// TODO too long argument list
		public ParallelManagerImpl(double start, double end, CyclicBarrier barrier,
				AtomicReference<List<Object>> aggregationList, boolean isMainThread)
		{
			this.start = start;
			this.end = end;
			this.barrier = barrier;
			this.aggregationList = aggregationList;
			this.isMainThread = isMainThread;
		}

		public ParallelManagerImpl(double start, double end, CyclicBarrier barrier,
				AtomicReference<List<Object>> aggregationList)
		{
			this(start, end, barrier, aggregationList, false);
		}

		@Override
		public Iterable<Integer> range(int startIndex, int endIndex, boolean sync)
		{
			if (sync)
			{
				sync();
			}
			int range = endIndex - startIndex;
			int startOfPart = startIndex + (int) (range * start);
			int endOfPart = startIndex + (int) (range * end);
			return Range.range(startOfPart, endOfPart);
		}

		// TODO do something with generics
		@Override
		public <E> E accumulate(Reducer<E, E> reducer, E result)
		{
			aggregationList.compareAndSet(null, new ArrayList<Object>());
			List<E> list = (List<E>) aggregationList.get();
			synchronized (aggregationList)
			{
				list.add(result);
			}
			sync();
			aggregationList.compareAndSet((List<Object>) list, null);
			return reduce(reducer, list);
		}

		@Override
		public boolean isMainThread()
		{
			sync();
			return isMainThread;
		}

		private void sync()
		{
			try
			{
				barrier.await();
			}
			catch (Exception e)
			{
				throw Throwables.propagate(e);
			}
		}

	}
}
