package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.WebhookParser;

@Component
public class BuildkiteWebhookParser implements WebhookParser {
    @Override
    public boolean supports(String provider) {
        return "buildkite".equals(provider);
    }

    @Override
    public BuildEvent parse(String payload) {
        // TODO: parse Buildkite payload; stubbed for now.
        return new BuildEvent(null, null, null, null);
    }
}
