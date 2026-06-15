package com.dillon.cianalyst.core;

public interface FailureAnalyzer {
    AnalysisResult analyze(BuildLog log);
}
