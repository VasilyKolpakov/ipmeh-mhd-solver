package ru.vasily.mydi;

public interface DIConfig {
	<T> Class<? extends T> getImpl(Class<T> clazz);
}
