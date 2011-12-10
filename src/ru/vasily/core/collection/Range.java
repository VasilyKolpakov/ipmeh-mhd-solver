package ru.vasily.core.collection;

import java.util.Iterator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Range implements Iterable<Integer>
{
	private final int start;
	private final int end;

	public static Iterable<Integer> range(int start, int end)
	{
		return new Range(start, end);
	}

	private Range(int start, int end)
	{
		Preconditions.checkArgument(start <= end, "start = %s > end = %s",
				start, end);
		this.start = start;
		this.end = end;
	}

	@Override
	public Iterator<Integer> iterator()
	{
		return new RangeIterator(start, end);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper("Range").
				add("start", start).
				add("end", end).
				toString();
	}

	private static class RangeIterator implements Iterator<Integer>
	{
		private final int end;
		private int current;

		public RangeIterator(int start, int end)
		{
			this.end = end;
			current = start;
		}

		@Override
		public boolean hasNext()
		{
			return current < end;
		}

		@Override
		public Integer next()
		{
			return current++;
		}

		@Override
		public void remove()
		{
		}
	}
}
