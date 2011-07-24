package ru.vasily.solverhelper;

import static org.junit.Assert.*;

import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import ru.vasily.solverhelper.misc.Callback;

import com.google.common.collect.ImmutableMap;

import static org.mockito.Mockito.*;

public class ParameterRetrievingResultHandlerTest {
	private static final String MAX_X = "max_x";
	private static final String MIN_X = "min_x";
	private static final String MAX_Y = "max_y";
	private static final String MIN_Y = "min_y";
	private static final String VALUE_NAME = "value_name";

	private ParameterRetrievingResultHandler handler;
	private ResultHandler rawDataOutput;
	private Callback<Map<String, String>> paramsHandler;

	@Before
	public void setup() {
		rawDataOutput = mock(ResultHandler.class);
		paramsHandler = mock(Callback.class);
		handler = new ParameterRetrievingResultHandler(paramsHandler);
	}

	@Test
	public void delegating_to_raw_output() {
		handler.handleResult1D("name", vals(1, 2), vals(0, 1));
		verify(rawDataOutput).handleResult1D("name", vals(1, 2), vals(0, 1));
	}

	@Test
	public void pass_params_to_template_manager() {
		handler.handleResult1D("name", vals(1, 2), vals(0, 1));
		verify(paramsHandler).call(
				ImmutableMap.<String, String> builder()
						.put(MAX_X, "2")
						.put(MIN_X, "1")
						.put(MAX_Y, "1")
						.put(MIN_Y, "0")
						.put(VALUE_NAME, "name")
						.build()
				);
	}

	private double[] vals(double... vals) {
		return vals;
	}
}
