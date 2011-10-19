package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ParallelEngineTest
{
	private List<Long> randomNumbers;

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
		double notParallelTime = System.currentTimeMillis();
		double notParallelSum = notParallelSum();
		notParallelTime = System.currentTimeMillis() - notParallelTime;

		double parallelTime = System.currentTimeMillis();
		double actualSum = parallelSum();
		parallelTime = System.currentTimeMillis() - parallelTime;

		System.out.println("not parallel time = " + notParallelTime);
		System.out.println("parallel time = " + parallelTime);
		System.out.println("ratio = " + (notParallelTime / parallelTime));
		
		assertEquals(notParallelSum, actualSum);
	}

	private double notParallelSum()
	{
		double sum = 0;
		long size = randomNumbers.size();
		for (int i = 0; i < size; i++)
		{
			sum += randomNumbers.get(i);
		}
		return sum;
	}

	private double parallelSum()
	{
		ParallelEngine engine = new ParallelEngine(3);
		List<Long> intermediateResults = Collections.synchronizedList(new ArrayList<Long>());
		SumTask task = new SumTask(intermediateResults);
		engine.run(task);
		double actualSum = 0;
		for (Long num : intermediateResults)
		{
			actualSum += num;
		}
		return actualSum;
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
