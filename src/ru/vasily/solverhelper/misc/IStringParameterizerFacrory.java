package ru.vasily.solverhelper.misc;

import java.util.Map;

import ru.vasily.solverhelper.misc.IStringParameterizerFacrory.StringParameterizer;

public interface IStringParameterizerFacrory {

	StringParameterizer getStringParameterizer(String bra, String cket, Map<String, String> params);

	public interface StringParameterizer {
		String insertParams(String s);
	}
}
