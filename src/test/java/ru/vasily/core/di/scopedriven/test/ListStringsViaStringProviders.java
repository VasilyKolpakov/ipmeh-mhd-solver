package ru.vasily.core.di.scopedriven.test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.vasily.core.di.DIKey;

import javax.annotation.Nullable;
import java.util.List;

public class ListStringsViaStringProviders
{
    private final List<StringProvider> stringProviders;

    public ListStringsViaStringProviders(@DIKey("stringProviders") List<StringProvider> stringProviders)
    {
        this.stringProviders = stringProviders;
    }

    public List<String> getStrings()
    {
        return Lists.transform(stringProviders, new Function<StringProvider, String>()
        {
            @Override
            public String apply(@Nullable StringProvider input)
            {
                return input.getString();
            }
        });
    }
}
