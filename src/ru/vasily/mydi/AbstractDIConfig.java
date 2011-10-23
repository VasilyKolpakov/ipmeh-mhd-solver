package ru.vasily.mydi;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDIConfig implements DIConfig
{
	private Map<Class<?>, Object> impls = new HashMap<Class<?>, Object>();

	public AbstractDIConfig()
	{
		initConfig();
	}

	public abstract void initConfig();

	public void addImplIgnoringInterface(Class<?> clazz)
	{
		registerComponent(clazz, clazz);
	}
	public void addImpl(Class<?> clazz)
	{
		registerComponent(clazz, clazz);
		for (Class<?> interf : clazz.getInterfaces())
		{
			registerComponent(interf, clazz);
		}
	}

	public void addObject(Object obj)
	{
		Class<? extends Object> clazz = obj.getClass();
		registerComponent(clazz, obj);
		for (Class<?> interf : clazz.getInterfaces())
		{
			registerComponent(interf, obj);
		}
	}
	

	private void registerComponent(Class<?> keyClass, Object impl)
	{
		if (impls.keySet().contains(keyClass))
		{
			throw new RuntimeException(
					"Duplicate implementations for key class = "
							+ keyClass.getCanonicalName() + " impl = {"
							+ impls.get(keyClass).toString() + ", "
							+ impl.toString() + "}");
		}
		impls.put(keyClass, impl);
	}

	@Override
	public Object getImpl(Class<?> clazz)
	{
		return impls.get(clazz);
	}
}
