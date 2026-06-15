package com.dillon.cianalyst.app;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.WebhookParser;

public class BuildkiteWebhookParser implements WebhookParser {
    @Override
    public boolean supports(String provider) {
        return "buildkite".equals(provider);
    }

    @Override
    public BuildEvent parse(String payload) {
        BuildEvent event = new BuildEvent();
        
        return event;
    }
}
