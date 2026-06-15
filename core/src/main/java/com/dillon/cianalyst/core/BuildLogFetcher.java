package com.dillon.cianalyst.core;

public interface BuildLogFetcher {
    BuildLog fetch(BuildEvent event);
}
