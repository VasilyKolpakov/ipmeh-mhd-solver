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
		Class<? extends T> implClass = config.getImpl(clazz);
		if (implClass == null) {
			throw new RuntimeException(
					"no registered implementation for class "
							+ clazz.getCanonicalName());
		}
		if (cycleGuard.contains(implClass)) {
			throw new RuntimeException(
					"cycling dependency! for interface class = "
							+ clazz.getCanonicalName()
							+ " , implementation class = "
							+ implClass.getCanonicalName());
		}
		cycleGuard.add(implClass);
		Object inst = instances.get(implClass);
		if (inst != null) {
			return (T) inst;
		}
		T newInstance = createNewInstance(implClass, cycleGuard);
		instances.put(implClass, newInstance);
		cycleGuard.remove(implClass);
		return newInstance;
	}

	@SuppressWarnings("unchecked")
	private <T> T createNewInstance(Class<T> implClass,
			HashSet<Class<?>> cycleGuard) {
		Constructor<T>[] constrs = (Constructor<T>[]) implClass
				.getConstructors();
		if (constrs.length != 1) {
			throw new RuntimeException(
					"number of constructors is not 1 for class "
							+ implClass.getCanonicalName()
							+ " num of constructors = " + constrs.length);
		}
		Constructor<T> cons = constrs[0];
		Class<?>[] paramTypes = cons.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = getInstanceViaDI(paramTypes[i], cycleGuard);
		}
		try {
			return cons.newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
