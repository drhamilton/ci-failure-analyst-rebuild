package com.dillon.cianalyst.core;

public record AnalysisResult(Long id, BuildEvent event, String category, String rootCause, String summary) {}
