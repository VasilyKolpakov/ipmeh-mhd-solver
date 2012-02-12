package ru.vasily.application.misc;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import static com.google.common.base.Preconditions.*;

public class StringParameterizerFactory implements IStringParameterizerFacrory
{
    private ConcurrentMap<String, Pattern> patterns = new MapMaker()
            .concurrencyLevel(1).makeComputingMap(
                    new Function<String, Pattern>()
                    {

                        @Override
                        public Pattern apply(String input)
                        {
                            return Pattern.compile(input);
                        }
                    });

    @Override
    public StringParameterizer getStringParameterizer(String bra, String cket,
                                                      Map<String, String> params)
    {
        Pattern pattern = patterns.get(createPatternString(params.keySet(),
                                                           bra, cket));
        return new StringParameterizerImpl(params, pattern);
    }

    private String createPatternString(Set<String> params, String bra,
                                       String cket)
    {
        checkArgument(!params.isEmpty(), "empty params");
        StringBuilder sb = new StringBuilder();
        sb.append("\\Q");
        sb.append(bra);
        sb.append("\\E(");
        Iterator<String> iter = params.iterator();
        sb.append("\\Q");
        sb.append(iter.next());
        sb.append("\\E");
        while (iter.hasNext())
        {
            sb.append("|");
            sb.append("\\Q");
            sb.append(iter.next());
            sb.append("\\E");
        }
        sb.append(")\\Q");
        sb.append(cket);
        sb.append("\\E");
        return sb.toString();
    }

    private static class StringParameterizerImpl implements StringParameterizer
    {

        private final Pattern pattern;
        private final Map<String, String> params;

        public StringParameterizerImpl(Map<String, String> params,
                                       Pattern pattern)
        {
            this.params = params;
            this.pattern = pattern;
        }

        @Override
        public String insertParams(String s)
        {
            Matcher matcher = pattern.matcher(s);
            StringBuffer sb = new StringBuffer();
            while (matcher.find())
            {
                String group = matcher.group(1);
                String replacement = params.get(group);
                matcher.appendReplacement(sb, "");
                sb.append(replacement);
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
    }
}
