package ru.vasily.core.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import static ru.vasily.core.collection.Reducers.*;

public class ParallelEngineTest
{
    private static final int NUMBER_OF_THREADS = 3;
    private List<Long> numbers;
    FutureBasedParallelEngine engine = new FutureBasedParallelEngine(NUMBER_OF_THREADS);

    @Before
    public void setup()
    {
        numbers = new ArrayList<Long>();
        for (int i = 0; i < 60000; i++)
        {
            numbers.add((long) i);
        }
        numbers = Collections.unmodifiableList(numbers);
    }

    @Test
    public void checkSum()
    {
        long notParallelSum = notParallelSum();
        long actualSum = parallelSum();
        assertEquals(notParallelSum, actualSum);
    }

    @Test(expected = TestPassedException.class)
    public void exception_is_throwed_from_main_thread()
    {
        engine.run(new AuxThreadFail());
    }

    @Test(expected = TestPassedException.class)
    public void sync_after_exception_do_not_make_deadlock()
    {
        engine.run(new DeadLockProneTask());
    }

    @Test
    public void sync()
    {
        AtomicInteger counter = new AtomicInteger(0);
        engine.run(new SyncTest(counter));
    }

    @Test
    public void sum_using_accumulate()
    {
        final long notParallelSum = notParallelSum();
        engine.run(new SmartParallelTask()
        {

            @Override
            public void doTask(ParallelManager par)
            {
                long sum = 0;
                for (int i : par.range(0, numbers.size(), true))
                {
                    sum += numbers.get(i);
                }
                assertThat(par.accumulate(longSum(), sum), equalTo(notParallelSum));
            }
        });
    }

    private long notParallelSum()
    {
        long sum = 0;
        long size = numbers.size();
        for (int i = 0; i < size; i++)
        {
            sum += numbers.get(i);
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
            int size = numbers.size();
            long sum = 0;
            for (int i : par.range(0, size, true))
            {
                sum += numbers.get(i);
            }
            intermediateResults.add(sum);
        }
    }

    private static class AuxThreadFail implements SmartParallelTask
    {
        @Override
        public void doTask(ParallelManager par)
        {
            for (int i : par.range(0, 10, true))
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
            par.range(0, 0, true);
            assertThat("calculated number of worker threads is equal to the actual one",
                       counter.get(), equalTo(NUMBER_OF_THREADS));
        }
    }

    public class DeadLockProneTask implements SmartParallelTask
    {

        @Override
        public void doTask(ParallelManager par)
        {
            for (int i : par.range(0, 10, true))
            {
                if (i == 9)
                {
                    // this will not be invoked in main thread
                    throw new TestPassedException();
                }
            }
            par.range(0, 1, true);
        }
    }

    @SuppressWarnings("serial")
    private static class TestPassedException extends RuntimeException
    {
    }
}
