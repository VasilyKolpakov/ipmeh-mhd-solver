package ru.vasily.core.collection;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import static com.google.common.collect.Lists.*;
import static ru.vasily.core.collection.Range.*;

public class RangeTest
{
	@Test
	public void simpleCase()
	{
		List<Integer> expected = Arrays.asList(0, 1, 2, 3);
		List<Integer> actual = newArrayList(range(0, 4));
		Assert.assertEquals(expected, actual);
	}
}
