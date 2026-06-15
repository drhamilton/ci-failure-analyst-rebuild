package com.dillon.cianalyst.core;

import java.util.List;

public interface AnalysisResultStore {
    AnalysisResult save(AnalysisResult result);
    List<AnalysisResult> findAll();
}
