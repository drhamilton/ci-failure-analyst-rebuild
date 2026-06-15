package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

@Component
public class ConsoleNotifier {
    public void send(AnalysisResult result) {
        System.out.println("=== BUILD FAILURE ANALYSIS ===");
        System.out.println("Repo:       " + result.event.repo);
        System.out.println("Branch:     " + result.event.branch);
        System.out.println("Category:   " + result.category);
        System.out.println("Root cause: " + result.rootCause);
        System.out.println("Summary:    " + result.summary);
    }
}
