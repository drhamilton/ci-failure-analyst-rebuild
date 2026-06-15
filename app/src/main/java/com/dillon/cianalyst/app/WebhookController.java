package com.dillon.cianalyst.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final FailureAnalysisService service;

    @PostMapping("/webhook/{provider}")
    public String receive(@PathVariable String provider, @RequestBody String payload) {
        service.analyze(provider, payload);
        return "ok";
    }
}
