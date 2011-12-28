package ru.vasily.core.parallel;

public final class ParallelTaskUtils
{
	public static int getIndexOfPart(int start, int end, double fraction)
	{
		return (int) (start + (end - start) * fraction);
	}
}
