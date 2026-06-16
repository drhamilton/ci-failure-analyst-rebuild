package com.dillon.cianalyst.app;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ResultsController {
    private final AnalysisResultStore store;

    @GetMapping("/results")
    public List<AnalysisResult> list() {
        return store.findAll();
    }
}
