package ru.vasily.dataobjs;

import java.io.Serializable;
import java.util.Map;

public class DataObj implements Serializable {
	/**
	 * 
	 */
	public static final String MAX_X = "max_x";
	public static final String MIN_X = "min_x";
	public static final String MAX_Y = "max_y";
	public static final String MIN_Y = "min_y";
	public static final String VALUE_NAME = "value_name";

	
	private static final long serialVersionUID = 1L;
	private double[] valueArray;
	private String key;
	private double[] xArray;
	private Map<String, String> params;

	public DataObj(String key, double[] valueArray, double[] xArray,
			Map<String, String> params) {
		super();
		this.valueArray = valueArray;
		this.key = key;
		this.xArray = xArray;
		this.params = params;
	}

	public DataObj() {
	}

	public double[] getArray() {
		return valueArray;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getKey() {
		return key;
	}

	public double[] getxArray() {
		return xArray;
	}

}
