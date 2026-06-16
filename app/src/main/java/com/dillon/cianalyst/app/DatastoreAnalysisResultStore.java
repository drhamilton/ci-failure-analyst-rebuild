package com.dillon.cianalyst.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.BuildEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatastoreAnalysisResultStore implements AnalysisResultStore {
    private final AnalysisResultRepository repository;

    @Override
    public AnalysisResult save(AnalysisResult result) {
        AnalysisResultEntity saved = repository.save(toEntity(result));
        return toDomain(saved);
    }

    @Override
    public List<AnalysisResult> findAll() {
        List<AnalysisResult> results = new ArrayList<>();
        repository.findAll().forEach(entity -> results.add(toDomain(entity)));
        return results;
    }

    private AnalysisResultEntity toEntity(AnalysisResult result) {
        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.repo = result.event.repo;
        entity.branch = result.event.branch;
        entity.category = result.category;
        entity.rootCause = result.rootCause;
        entity.summary = result.summary;
        return entity;
    }
    
    private AnalysisResult toDomain(AnalysisResultEntity entity) {
        BuildEvent event = new BuildEvent();
        event.repo = entity.repo;
        event.branch = entity.branch;

        AnalysisResult result = new AnalysisResult();
        result.event = event;
        result.category = entity.category;
        result.rootCause = entity.rootCause;
        result.summary = entity.summary;
        return result;
    }
}
