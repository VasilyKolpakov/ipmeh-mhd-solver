package ru.vasily.mydi;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MyDI {
	private final DIConfig config;
	private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

	public MyDI(DIConfig config) {
		this.config = config;
	}

	public <T> T getInstanceViaDI(Class<T> clazz) {
		return getInstanceViaDI(clazz, new HashSet<Class<?>>());
	}

	private <T> T getInstanceViaDI(Class<T> clazz, HashSet<Class<?>> cycleGuard) {
		Object impl = config.getImpl(clazz);
		if (!(impl instanceof Class))
		{
			return (T) impl;
		}
		Class<T> implClass = (Class<T>) impl;
		if (implClass == null)
		{
			throw new RuntimeException(
					"no registered implementation for class "
							+ clazz.getCanonicalName());
		}
		if (cycleGuard.contains(implClass))
		{
			throw new RuntimeException(
					"cycling dependency! for interface class = "
							+ clazz.getCanonicalName()
							+ " , implementation class = "
							+ implClass.getCanonicalName());
		}
		cycleGuard.add(implClass);
		T inst = (T) instances.get(implClass);
		if (inst == null)
		{
			inst = createNewInstance(implClass, cycleGuard);
			instances.put(implClass, inst);
		}
		cycleGuard.remove(implClass);
		return inst;
	}

	@SuppressWarnings("unchecked")
	private <T> T createNewInstance(Class<T> implClass,
			HashSet<Class<?>> cycleGuard) {
		Constructor<T>[] constrs = (Constructor<T>[]) implClass
				.getConstructors();
		if (constrs.length != 1)
		{
			throw new RuntimeException(
					"number of constructors is not 1 for class "
							+ implClass.getCanonicalName()
							+ " num of constructors = " + constrs.length);
		}
		Constructor<T> cons = constrs[0];
		Class<?>[] paramTypes = cons.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < params.length; i++)
		{
			params[i] = getInstanceViaDI(paramTypes[i], cycleGuard);
		}
		try
		{
			return cons.newInstance(params);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
