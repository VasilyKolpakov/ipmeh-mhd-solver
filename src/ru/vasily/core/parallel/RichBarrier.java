package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Throwables;

public class RichBarrier
{

	@SuppressWarnings("serial")
	private static final class OtherThreadFailException extends RuntimeException
	{
	}

	private static final OtherThreadFailException OTHER_THREAD_FAIL_EXCEPTION = new OtherThreadFailException();
	private final CyclicBarrier barrier;
	private final AtomicReference<List<?>> aggregationList = new AtomicReference<List<?>>();
	private final AtomicBoolean thereWasException = new AtomicBoolean(false);

	public static RichBarrier createRichBarrier(int numberOfThreads)
	{
		CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
		return new RichBarrier(barrier);
	}

	private RichBarrier(CyclicBarrier barrier)
	{
		this.barrier = barrier;
	}

	public void await()
	{
		try
		{
			barrier.await();
			checkFail();
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
	}

	private void checkFail()
	{
		if (thereWasException.get())
		{
			throw OTHER_THREAD_FAIL_EXCEPTION;
		}
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> collectDataFromThreads(E data)
	{
		aggregationList.compareAndSet(null, new ArrayList<Object>());
		List<E> list = (List<E>) aggregationList.get();
		synchronized (aggregationList)
		{
			list.add(data);
		}
		await();
		aggregationList.compareAndSet(list, null);
		return list;
	}

	private void runTask(RichBarrierTask task)
	{
		try
		{
			task.barrierTask();
			await();
		}
		catch (OtherThreadFailException e)
		{
			// silent fail because the other thread is already waiting to throw the exception 
			return;
		}
		catch (RuntimeException e)
		{
			thereWasException.compareAndSet(false, true);
			try
			{
				barrier.await();
			}
			catch (Exception ex)
			{
				throw Throwables.propagate(ex);
			}
			throw e;
		}
	}

	public Runnable asRunnable(RichBarrierTask task)
	{
		return new ThreadNexusTaskWrapper(task);
	}

	private class ThreadNexusTaskWrapper implements Runnable
	{

		private final RichBarrierTask task;

		public ThreadNexusTaskWrapper(RichBarrierTask task)
		{
			this.task = task;
		}

		@Override
		public void run()
		{
			runTask(task);
		}
	}

}