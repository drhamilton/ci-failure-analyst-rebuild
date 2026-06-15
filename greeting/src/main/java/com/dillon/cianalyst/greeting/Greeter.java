package com.dillon.cianalyst.greeting;

import org.springframework.stereotype.Component;

@Component
public class Greeter {
    public String greet() {
        return "Hello";
    }
}