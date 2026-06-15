package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

@Component
public class GithubWebhookParser {
    public BuildEvent parse(String rawPaylod) {
        BuildEvent event = new BuildEvent();

        event.id = "run-123";
        event.repo = "acme/payments-service";
        event.branch = "main";
        event.status = "failed";

        return event;
    }
}
