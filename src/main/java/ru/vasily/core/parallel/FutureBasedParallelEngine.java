package ru.vasily.core.parallel;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import ru.vasily.core.collection.Range;
import ru.vasily.core.collection.Reducer;

import static ru.vasily.core.collection.Reducers.*;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class FutureBasedParallelEngine implements ParallelEngine
{
    private final ExecutorService executor;
    private final double fraction;
    private final int numberOfThreads;
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);

    public FutureBasedParallelEngine(int numberOfThreads)
    {
        this.numberOfThreads = numberOfThreads;
        checkArgument(numberOfThreads > 0, "number of threads must be > 0");
        fraction = 1.0 / numberOfThreads;
        checkState(fraction * numberOfThreads == 1.0,
                   "rounding problem: fraction * numberOfThreads != 1.0, numberOfThreads = %s",
                   numberOfThreads);
        executor = Executors.newCachedThreadPool(new ThreadFactory()
        {

            @Override
            public Thread newThread(Runnable r)
            {
                String name = "FutureBasedParallelEngine thread #" + THREAD_COUNTER.getAndIncrement();
                Thread t = new Thread(r, name);
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

    @Override
    public void run(SmartParallelTask task)
    {
        RichBarrier richBarrier = RichBarrier.createRichBarrier(numberOfThreads);
        List<Future<?>> futures = Lists.newArrayListWithCapacity(numberOfThreads - 1);
        for (int i = 1; i < numberOfThreads; i++)
        {
            ParallelManager par = new ParallelManagerImpl(fraction * i, fraction * (i + 1),
                                                          richBarrier);
            RichBarrierTask barrierTask = new BarrierTaskImpl(par, task);
            Future<?> future = executor.submit(richBarrier.asRunnable(barrierTask));
            futures.add(future);
        }
        ParallelManager par = new ParallelManagerImpl(0, fraction, richBarrier, true);
        RichBarrierTask barrierTask = new BarrierTaskImpl(par, task);
        richBarrier.asRunnable(barrierTask).run();
        waitForOtherThreads(futures);
    }

    private static class BarrierTaskImpl implements RichBarrierTask
    {
        private final SmartParallelTask task;
        private final ParallelManager par;

        public BarrierTaskImpl(ParallelManager par, SmartParallelTask task)
        {
            this.par = par;
            this.task = task;
        }

        @Override
        public void barrierTask()
        {
            task.doTask(par);
        }
    }

    private static class ParallelManagerImpl implements ParallelManager
    {

        private final double start;
        private final double end;
        private final boolean isMainThread;
        private final RichBarrier richBarrier;

        public ParallelManagerImpl(double start, double end, RichBarrier richBarrier,
                                   boolean isMainThread)
        {
            this.start = start;
            this.end = end;
            this.richBarrier = richBarrier;
            this.isMainThread = isMainThread;
        }

        public ParallelManagerImpl(double start, double end, RichBarrier threadNexus)
        {
            this(start, end, threadNexus, false);
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

        @Override
        public <E> E accumulate(Reducer<E, E> reducer, E result)
        {
            List<E> results = richBarrier.collectDataFromThreads(result);
            return reduce(reducer, results);
        }

        @Override
        public boolean isMainThread()
        {
            sync();
            return isMainThread;
        }

        private void sync()
        {
            richBarrier.await();
        }

    }
}
