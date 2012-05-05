package ru.vasily.core.di.scopedriven;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.vasily.core.di.DIKey;
import ru.vasily.core.di.scopedriven.test.*;

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

    @Test
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
        new ScopeDrivenDI(module).getInstance(StringServiceWithProvider.class);
    }

    @Test
    public void meaningful_exception_message_on_cyclic_dependency()
    {
        expectedEx.expect(RuntimeException.class);
        Matcher<String> expectedMessage = allOf(
                containsString(TopComponent.class.getSimpleName()),
                containsString("service1"),
                containsString(StringServiceWithTopComponentDependency.class.getSimpleName())
                                               );
        expectedEx.expectMessage(expectedMessage);

        String providedString = "Hello";
        SDModule module = MapSDModule.builder()
                                     .putComplexComponent("service1", StringServiceWithTopComponentDependency.class)
                                     .putComplexComponent("service2", StringServiceWithProvider.class)
                                     .putComplexComponent("stringProvider", StringProvider.class)
                                     .putPrimitive("string", providedString)
                                     .build();

        new ScopeDrivenDI(module).getInstance(TopComponent.class);
    }

    @Test
    public void meaningful_exception_message_on_non_marked_constructor_parameters()
    {
        expectedEx.expect(RuntimeException.class);
        Matcher<String> expectedMessage = allOf(
                containsString(ClassWithUnmarkedConstructor.class.getSimpleName()),
                containsString("constructor"),
                containsString(DIKey.class.getSimpleName())
                                               );
        expectedEx.expectMessage(expectedMessage);
        new ScopeDrivenDI(SDModule.EMPTY_MODULE).getInstance(ClassWithUnmarkedConstructor.class);
    }
}
