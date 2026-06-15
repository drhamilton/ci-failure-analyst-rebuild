package com.dillon.cianalyst.app;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final GithubWebhookParser parser;
    private final GithubLogFetcher logFetcher;
    private final FailureAnalyzer analyzer;
    private final ConsoleNotifier notifier;

    @PostMapping("/webhook")
    public String receive(@RequestBody String payload) {
        BuildEvent event = parser.parse(payload);
        BuildLog log = logFetcher.fetch(event);
        AnalysisResult result = analyzer.analyze(log);
        notifier.send(result);
        return "ok";
    }
}
