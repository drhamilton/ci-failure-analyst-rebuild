package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.BuildLogFetcher;

@Component
public class GithubLogFetcher implements BuildLogFetcher {
    @Override
    public BuildLog fetch(BuildEvent event) {
        return new BuildLog(event, "Failed: nullpointerexception at paymentservice.java");
    }

}
