package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.FailureAnalyzer;

@Component
public class StubFailureAnalyzer implements FailureAnalyzer {

    @Override
    public AnalysisResult analyze(BuildLog log) {
        return new AnalysisResult(
            null,
            log.event(),
            "TEST_FAILURE",
            "NullPointerException at PaymentService.java:42",
            "A test failed due to an unexpected null value.");
    }
}
