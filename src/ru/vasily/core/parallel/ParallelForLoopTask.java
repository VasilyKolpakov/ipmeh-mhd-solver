package ru.vasily.core.parallel;

public class ParallelForLoopTask implements ParallelTask
{

	private final int beginIndex;
	private final int endIndex;
	private final LoopBody loopBody;

	public ParallelForLoopTask(int beginIndex, int endIndex, LoopBody loopBody)
	{
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.loopBody = loopBody;
	}

	@Override
	public final void doPart(double start, double end)
	{
		int startI = ParallelTaskUtils.getIndexOfPart(beginIndex, endIndex, start);
		int finishI = ParallelTaskUtils.getIndexOfPart(beginIndex, endIndex, end);
		for (int i = startI; i < finishI; i++)
		{
			loopBody.loopBody(i);
		}
	}

	public interface LoopBody
	{
		void loopBody(int i);
	}

}
