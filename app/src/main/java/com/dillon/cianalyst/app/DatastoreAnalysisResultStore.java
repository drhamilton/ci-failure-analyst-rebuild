package com.dillon.cianalyst.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.BuildEvent;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!aws")
@RequiredArgsConstructor
public class DatastoreAnalysisResultStore implements AnalysisResultStore {
    private final AnalysisResultRepository repository;
    private final OutboxRepository outboxRepository;

    @Override
    @Transactional
    public AnalysisResult save(AnalysisResult result) {
        AnalysisResultEntity saved = repository.save(toEntity(result));
        outboxRepository.save(pendingOutbox(saved.id));
        return toDomain(saved);
    }

    private OutboxEntity pendingOutbox(Long referenceId) {
        OutboxEntity row = new OutboxEntity();
        row.referenceId = referenceId;
        row.status = OutboxStatus.PENDING;
        row.createdAt = Instant.now();
        row.attempts = 0;
        return row;
    }

    @Override
    public List<AnalysisResult> findAll() {
        List<AnalysisResult> results = new ArrayList<>();
        repository.findAll().forEach(entity -> results.add(toDomain(entity)));
        return results;
    }

    @Override
    public Optional<AnalysisResult> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private AnalysisResultEntity toEntity(AnalysisResult result) {
        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.repo = result.event().repo();
        entity.branch = result.event().branch();
        entity.category = result.category();
        entity.rootCause = result.rootCause();
        entity.summary = result.summary();
        return entity;
    }

    private AnalysisResult toDomain(AnalysisResultEntity entity) {
        BuildEvent event = new BuildEvent(null, entity.repo, entity.branch, null);
        return new AnalysisResult(
            entity.id, event, entity.category, entity.rootCause, entity.summary);
    }
}
