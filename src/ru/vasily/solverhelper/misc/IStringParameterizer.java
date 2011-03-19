package ru.vasily.solverhelper.misc;

import java.util.Map;

public interface IStringParameterizer {
	String insertParams(String s, Map<String, String> params);
}
