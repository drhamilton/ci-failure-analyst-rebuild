package com.dillon.cianalyst.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final FailureAnalysisService service;
    private final GithubWebhookVerifier verifier;

    @PostMapping("/webhook/{provider}")
    public String receive(
            @PathVariable String provider, 
            @RequestBody String payload, 
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        if ("github".equals(provider)) {
         verifier.verify(payload, signature);
        } else {
            throw new UnsupportedProviderException(provider);
        }
        service.analyze(provider, payload);
        return "ok";
   }
}
