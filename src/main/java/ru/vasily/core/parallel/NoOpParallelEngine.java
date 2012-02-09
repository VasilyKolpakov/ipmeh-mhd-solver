package ru.vasily.core.parallel;

import ru.vasily.core.collection.Reducer;
import ru.vasily.core.collection.Range;

public class NoOpParallelEngine implements ParallelEngine
{
    public static final ParallelEngine INSTANCE = new NoOpParallelEngine();
    private static final ParallelManager NO_OP_PARALLEL_MANAGER = new ParallelManagerImplementation();

    @Override
    public void run(SmartParallelTask task)
    {
        task.doTask(NO_OP_PARALLEL_MANAGER);
    }

    private static final class ParallelManagerImplementation implements ParallelManager
    {
        @Override
        public Iterable<Integer> range(int start, int end, boolean sync)
        {
            return Range.range(start, end);
        }

        @Override
        public boolean isMainThread()
        {
            return true;
        }

        @Override
        public <E> E accumulate(Reducer<E, E> reducer, E result)
        {
            return result;
        }
    }

}
