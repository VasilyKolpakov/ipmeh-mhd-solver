package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class ParallelEngineTest
{
	private static final int NUMBER_OF_THREADS = 3;
	private List<Long> randomNumbers;
	FutureBasedParallelEngine engine = new FutureBasedParallelEngine(NUMBER_OF_THREADS);

	@Before
	public void setup()
	{
		randomNumbers = new ArrayList<Long>();
		for (int i = 0; i < 6000000; i++)
		{
			randomNumbers.add((long) (Math.random() * 100));
		}
		randomNumbers = Collections.unmodifiableList(randomNumbers);
	}

	@Test
	public void checkSum()
	{
		long notParallelSum = notParallelSum();
		long actualSum = parallelSum();
		assertEquals(notParallelSum, actualSum);
	}

	@Test(expected = TestPassedException.class)
	public void exceptionHandling()
	{
		engine.run(new AuxThreadFail());
	}

	@Test
	public void sync()
	{
		AtomicInteger counter = new AtomicInteger(0);
		engine.run(new SyncTest(counter));
	}

	private long notParallelSum()
	{
		long sum = 0;
		long size = randomNumbers.size();
		for (int i = 0; i < size; i++)
		{
			sum += randomNumbers.get(i);
		}
		return sum;
	}

	private long parallelSum()
	{
		List<Long> intermediateResults = Collections.synchronizedList(new ArrayList<Long>());
		SumTask task = new SumTask(intermediateResults);
		engine.run(task);
		long actualSum = 0;
		for (Long num : intermediateResults)
		{
			actualSum += num;
		}
		return actualSum;
	}

	private class SumTask implements SmartParallelTask
	{
		private final List<Long> intermediateResults;

		public SumTask(List<Long> intermediateResults)
		{
			this.intermediateResults = intermediateResults;
		}

		@Override
		public void doTask(ParallelManager par)
		{
			int size = randomNumbers.size();
			long sum = 0;
			for (Integer i : par.range(0, size))
			{
				sum += randomNumbers.get(i);
			}
			intermediateResults.add(sum);
		}
	}

	private static class AuxThreadFail implements SmartParallelTask
	{
		@Override
		public void doTask(ParallelManager par)
		{
			for (Integer i : par.range(0, 10))
			{
				if (i == 9)
				{
					// this will not be invoked in main thread
					throw new TestPassedException();
				}
			}
		}
	}

	private static class SyncTest implements SmartParallelTask
	{

		private final AtomicInteger counter;

		public SyncTest(AtomicInteger counter)
		{
			this.counter = counter;
		}

		@Override
		public void doTask(ParallelManager par)
		{
			counter.incrementAndGet();
			par.sync();
			assertThat("calculated number of worker threads is equal to the actual one",
					counter.get(), Matchers.equalTo(NUMBER_OF_THREADS));
		}
	}

	private static class TestPassedException extends RuntimeException
	{
	}
}
