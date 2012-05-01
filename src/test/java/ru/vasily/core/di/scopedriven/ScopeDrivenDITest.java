package ru.vasily.core.di.scopedriven;

import org.junit.Test;
import ru.vasily.core.di.scopedriven.test.GreetingProvider;
import ru.vasily.core.di.scopedriven.test.HelloComp;
import ru.vasily.core.di.scopedriven.test.HelloCompWithLogging;
import ru.vasily.core.di.scopedriven.test.TopComponent;

public class ScopeDrivenDITest
{
    @Test
    public void simple_case()
    {
        SDModule comp1Module = MapSDModule.builder()
                .putComplexComponent("greetingProvider", GreetingProvider.class)
                .putPrimitive("greeting", "Welcome")
                .build();
        SDModule module = MapSDModule.builder()
                .putComplexComponent("comp1", HelloCompWithLogging.class, comp1Module)
                .putComplexComponent("comp2", HelloCompWithLogging.class)
                .putComplexComponent("greetingProvider", GreetingProvider.class)
                .putPrimitive("greeting", "Hello")
                .build();
        TopComponent topComponent = new ScopeDrivenDI(module).getInstance(TopComponent.class);
        topComponent.goAll();
    }
}
