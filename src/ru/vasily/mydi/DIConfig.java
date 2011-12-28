package ru.vasily.mydi;

public interface DIConfig {
	 Object getImpl(Class<?> clazz);
}
