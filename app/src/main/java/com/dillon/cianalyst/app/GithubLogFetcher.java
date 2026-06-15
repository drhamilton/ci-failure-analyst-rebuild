package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

@Component
public class GithubLogFetcher {
    public BuildLog fetch(BuildEvent event) {
        BuildLog log = new BuildLog();
        log.event = event;
        log.content = "Failed: nullpointerexception at paymentservice.java";
        return log;
    }

}
