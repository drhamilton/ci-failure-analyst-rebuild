package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

@Component
public class FailureAnalyzer {

    public AnalysisResult analyze(BuildLog log) {
        AnalysisResult result = new AnalysisResult();

        result.event = log.event;
        result.category = "TEST_FAILURE";
        result.rootCause = "NullPointerException at PaymentService.java:42";
        result.summary = "A test failed due to an unexpected null value.";

        return result;
    }

}
