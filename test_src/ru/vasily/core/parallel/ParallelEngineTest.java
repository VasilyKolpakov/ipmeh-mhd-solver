package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ru.vasily.core.parallel.ParallelForLoopTask.LoopBody;

public class ParallelEngineTest
{
	private List<Long> randomNumbers;
	ExecutorServiceBasedParallelEngine engine = new ExecutorServiceBasedParallelEngine(3);

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
		long notParallelTime = System.currentTimeMillis();
		long notParallelSum = notParallelSum();
		notParallelTime = System.currentTimeMillis() - notParallelTime;

		long parallelTime = System.currentTimeMillis();
		long actualSum = parallelSum();
		parallelTime = System.currentTimeMillis() - parallelTime;

		System.out.println("not parallel time = " + notParallelTime);
		System.out.println("parallel time = " + parallelTime);
		System.out.println("ratio = " + (notParallelTime / parallelTime));

		assertEquals(notParallelSum, actualSum);
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


	@Test
	public void checkSumForLoop()
	{
		long notParallelSum = notParallelSum();
		SumTask2 sumTask = new SumTask2();
		engine.run(new ParallelForLoopTask(0, randomNumbers.size(), sumTask));
		long actualSum = 0;
		for (Long l : sumTask.sum.values())
		{
			actualSum += l;
		}
		assertEquals(notParallelSum, actualSum);
	}

	private class SumTask2 implements LoopBody
	{
		public Map<Long, Long> sum = Collections.synchronizedMap(new HashMap<Long, Long>());

		@Override
		public void loopBody(int i)
		{
			long currentThreadId = Thread.currentThread().getId();
			if(sum.get(currentThreadId) == null)
			{
				sum.put(currentThreadId, 0l);
			}
			Long newSum = sum.get(currentThreadId) + randomNumbers.get(i);
			sum.put(currentThreadId, newSum);
		}
	}

	private class SumTask implements ParallelTask
	{
		private final List<Long> intermediateResults;

		public SumTask(List<Long> intermediateResults)
		{
			this.intermediateResults = intermediateResults;
		}

		@Override
		public void doPart(double start, double end)
		{
			System.out.println("start = " + start + " end = " + end);
			int size = randomNumbers.size();
			long sum = 0;
			for (int i = (int) (size * start); i < (int) (size * end); i++)
			{
				sum += randomNumbers.get(i);
			}
			intermediateResults.add(sum);
		}
	}
}
