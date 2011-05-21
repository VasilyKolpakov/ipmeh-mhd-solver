package ru.vasily.solverhelper.misc;

import java.util.Map;

public interface IStringParameterizerFacrory {

	StringParameterizer getStringParameterizer(String bra, String cket, Map<String, String> params);

	public interface StringParameterizer {
		String insertParams(String s);
	}
}
