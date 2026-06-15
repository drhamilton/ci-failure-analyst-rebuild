package com.dillon.cianalyst.core;

public interface WebhookParser {
    boolean supports(String provider);
    BuildEvent parse(String payload);
}