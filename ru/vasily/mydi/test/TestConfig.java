package ru.vasily.mydi.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;

import ru.vasily.mydi.DIConfig;
import ru.vasily.mydi.MyDI;

public class TestConfig implements DIConfig {
	private Map<Class, Class> impls = new HashMap<Class, Class>();

	public TestConfig() {
		impls.put(IA.class, A.class);
		impls.put(IB.class, B.class);
		impls.put(IC.class, C.class);
	}
	@Override
	public Class getImpl(Class clazz) {
		return impls.get(clazz);
	}

	public static void main(String[] args) {
		MyDI di = new MyDI(new TestConfig());
		IC c = di.getInstanceViaDI(IC.class);
		System.out.println(c.getA());
	}
}
