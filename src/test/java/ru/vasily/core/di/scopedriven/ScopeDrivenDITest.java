package ru.vasily.core.di.scopedriven;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.vasily.core.di.scopedriven.test.StringService;
import ru.vasily.core.di.scopedriven.test.StringServiceWithProvider;
import ru.vasily.core.di.scopedriven.test.StringProvider;
import ru.vasily.core.di.scopedriven.test.TopComponent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class ScopeDrivenDITest
{
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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

    @Test
    public void complex_module()
    {
        String providedString1 = "Hello1";
        String providedString2 = "Hello2";
        SDModule service2Module = MapSDModule.builder()
                                             .putComplexComponent("stringProvider", StringProvider.class)
                                             .putPrimitive("string", providedString2)
                                             .build();
        SDModule module = MapSDModule.builder()
                                     .putComplexComponent("service1", StringServiceWithProvider.class)
                                     .putComplexComponent("service2", StringServiceWithProvider.class, service2Module)
                                     .putComplexComponent("stringProvider", StringProvider.class)
                                     .putPrimitive("string", providedString1)
                                     .build();
        TopComponent topComponent = new ScopeDrivenDI(module).getInstance(TopComponent.class);
        assertThat(topComponent.getString1(), is(providedString1));
        assertThat(topComponent.getString2(), is(providedString2));
    }

    @Test()
    public void meaningful_exception_message_on_absence_of_named_component()
    {
        expectedEx.expect(RuntimeException.class);
        Matcher<String> expectedMessage = allOf(
                containsString(StringServiceWithProvider.class.getSimpleName()),
                containsString("stringProvider")
                                               );
        expectedEx.expectMessage(expectedMessage);
        SDModule module = MapSDModule.builder()
                                     .build();
        StringService stringService = new ScopeDrivenDI(module).getInstance(StringServiceWithProvider.class);
    }
}
