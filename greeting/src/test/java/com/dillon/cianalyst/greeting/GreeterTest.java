package com.dillon.cianalyst.greeting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class GreeterTest {
    @Test
    void greetReturnsExpectedMessage() {
        Greeter greeter = new Greeter();

        String result = greeter.greet();

        assertEquals("Hello", result);
    }
}