package ru.vasily.mydi;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDIConfig implements DIConfig {
	private Map<Class<?>, Class<?>> impls = new HashMap<Class<?>, Class<?>>();

	public AbstractDIConfig() {
		initConfig();
	}

	public abstract void initConfig();

	public void addImpl(Class<?> clazz) {
		registerComponent(clazz, clazz);
		for (Class<?> interf : clazz.getInterfaces()) {
			registerComponent(interf, clazz);
		}
	}

	private void registerComponent(Class<?> keyClass, Class<?> implClass) {
		if (impls.keySet().contains(keyClass)) {
			throw new RuntimeException(
					"Duplicate implementations for interface = "
							+ keyClass.getCanonicalName() + " impl classes = {"
							+ impls.get(keyClass).getCanonicalName() + ", "
							+ implClass.getCanonicalName() + "}");
		}
		impls.put(keyClass, implClass);
	}

	@Override
	public <T> Class<? extends T> getImpl(Class<T> clazz) {
		return (Class<? extends T>) impls.get(clazz);
	}
}
