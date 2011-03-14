package ru.vasily.solver;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class AlgorithmError extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<String, Object> params;

	public Map<String, Object> getParams() {
		return params;
	}

	public AlgorithmError(Map<String, Object> params) {
		this.params = params;
	}
	
	public static Builder builder() {
		return new AlErrBuilder();
	}

	public interface Builder {
		Builder put(String key, Object value);

		AlgorithmError build();
	}

	private static class AlErrBuilder implements Builder {
		ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap
				.builder();

		@Override
		public Builder put(String key, Object value) {
			mapBuilder.put(key, value);
			return this;
		}

		@Override
		public AlgorithmError build() {
			return new AlgorithmError(mapBuilder.build());
		}

	}
}
