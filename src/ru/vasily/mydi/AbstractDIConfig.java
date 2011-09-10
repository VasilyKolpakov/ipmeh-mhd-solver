package ru.vasily.mydi;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDIConfig implements DIConfig {
	private Map<Class<?>, Object> impls = new HashMap<Class<?>, Object>();

	public AbstractDIConfig() {
		initConfig();
	}

	public abstract void initConfig();

	protected void addImpl(Class<?> clazz) {
		registerComponent(clazz, clazz);
		for (Class<?> interf : clazz.getInterfaces())
		{
			registerComponent(interf, clazz);
		}
	}

	protected void registerComponent(Class<?> keyClass, Object impl) {
		if (impls.keySet().contains(keyClass))
		{
			throw new RuntimeException(
					"Duplicate implementations for interface = "
							+ keyClass.getCanonicalName() + " impl = {"
							+ impls.get(keyClass).toString() + ", "
							+ impl.toString() + "}");
		}
		impls.put(keyClass, impl);
	}

	@Override
	public Object getImpl(Class<?> clazz) {
		return impls.get(clazz);
	}
}
