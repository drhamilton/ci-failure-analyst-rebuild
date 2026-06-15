package com.dillon.cianalyst.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.dillon.cianalyst.greeting.Greeter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final Greeter greeter;

    @GetMapping("/hello")
    public String hello() {
        log.info("Handling hello");
        return greeter.greet();
    }
}
