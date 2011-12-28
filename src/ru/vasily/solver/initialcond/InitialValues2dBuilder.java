package ru.vasily.solver.initialcond;

import ru.vasily.solver.initialcond.Init2dFunction;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 10/5/11
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface InitialValues2dBuilder<T>  // TODO remove
{
	void apply(Init2dFunction function);

	T build();
}
