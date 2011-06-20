package ru.vasily.solverhelper.misc;

import static org.junit.Assert.*;

import org.junit.Test;

public class AreaMeterTest {

	@Test
	public void square() {
		double area = AreaMeter.area(
				0, 0,
				0, 1,
				1, 1,
				1, 0);
		assertEquals(1, area, 0.00000001);
	}

	@Test
	public void trapezoid() {
		double area = AreaMeter.area(
				0, 0,
				0, 1,
				1, 3,
				1, 0);
		assertEquals(2, area, 0.00000001);
	}
}
