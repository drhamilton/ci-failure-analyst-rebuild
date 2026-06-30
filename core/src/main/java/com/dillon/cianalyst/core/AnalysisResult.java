package com.dillon.cianalyst.core;

public record AnalysisResult(Long id, BuildEvent event, String category, String rootCause, String summary, String logKey) {
    public AnalysisResult withLogKey(String logKey) {
        return new AnalysisResult(id, event, category, rootCause, summary, logKey);
    }
}
