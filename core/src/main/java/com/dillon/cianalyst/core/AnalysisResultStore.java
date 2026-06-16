package com.dillon.cianalyst.core;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultStore {
    AnalysisResult save(AnalysisResult result);
    List<AnalysisResult> findAll();
    Optional<AnalysisResult> findById(Long id);
}
