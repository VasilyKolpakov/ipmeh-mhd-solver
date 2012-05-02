package ru.vasily.core.di.scopedriven;

import org.junit.Test;
import ru.vasily.core.di.scopedriven.test.StringServiceWithProvider;
import ru.vasily.core.di.scopedriven.test.StringProvider;
import ru.vasily.core.di.scopedriven.test.TopComponent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScopeDrivenDITest
{
    @Test
    public void simple_case()
    {
        String providedString = "Hello";
        SDModule module = MapSDModule.builder()
                                     .putComplexComponent("service1", StringServiceWithProvider.class)
                                     .putComplexComponent("service2", StringServiceWithProvider.class)
                                     .putComplexComponent("stringProvider", StringProvider.class)
                                     .putPrimitive("string", providedString)
                                     .build();
        TopComponent topComponent = new ScopeDrivenDI(module).getInstance(TopComponent.class);
        assertThat(topComponent.getString1(), is(providedString));
        assertThat(topComponent.getString2(), is(providedString));
    }
}
