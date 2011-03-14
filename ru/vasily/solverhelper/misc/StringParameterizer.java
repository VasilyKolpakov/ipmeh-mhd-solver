package ru.vasily.solverhelper.misc;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.*;

public class StringParameterizer implements IStringParameterizer {
	private static final Pattern DOLLAR_SIGN = Pattern.compile("[$]");
	private ConcurrentMap<String, Pattern> patterns = new MapMaker()
			.concurrencyLevel(1).makeComputingMap(
					new Function<String, Pattern>() {

						@Override
						public Pattern apply(String input) {
							return Pattern.compile(input);
						}
					});

	@Override
	public String insertParams(String s, Map<String, String> params) {
		for (String key : params.keySet()) {
			checkArgument(!DOLLAR_SIGN.matcher(params.get(key)).find(),
					"illegal character \'$\' in param = " + key + " value = "
							+ params.get(key));
		}
		Pattern pattern = patterns.get(createPatternString(params.keySet()));
		Matcher matcher = pattern.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String group = matcher.group(1);
			String replacement = params.get(group);
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String createPatternString(Set<String> params) {
		checkArgument(!params.isEmpty(), "empty params");
		StringBuilder sb = new StringBuilder();
		sb.append("\\[(");
		Iterator<String> iter = params.iterator();
		sb.append("\\Q");
		sb.append(iter.next());
		sb.append("\\E");
		while (iter.hasNext()) {
			sb.append("|");
			sb.append("\\Q");
			sb.append(iter.next());
			sb.append("\\E");
		}
		sb.append(")\\]");
		return sb.toString();
	}

	public static void main(String[] args) {
		StringParameterizer test = new StringParameterizer();
		String s = test.insertParams("asdasdasd=[b],asdaaqwe=[a]",
				ImmutableMap.of("b", "b_val", "a", "ae0_val"));
		System.out.println(s);
	}
}
