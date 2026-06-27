package com.dillon.cianalyst.app;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.BuildEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * DynamoDB-backed {@link AnalysisResultStore}, active under the {@code aws} profile.
 *
 * <p>DynamoDB has no server-side auto-increment, so unlike the Datastore adapter the
 * id is generated here. The {@code core} port keeps {@code Long}; a String/ULID id is
 * a deferred, separate slice. The outbox write the Datastore adapter performs is
 * intentionally omitted — that path becomes SQS in a later slice.
 */
@Component
@Profile("aws")
@RequiredArgsConstructor
public class DynamoAnalysisResultStore implements AnalysisResultStore {
    private final DynamoDbTable<DynamoAnalysisResultEntity> table;

    @Override
    public AnalysisResult save(AnalysisResult result) {
        DynamoAnalysisResultEntity entity = toEntity(result);
        entity.setId(ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        table.putItem(entity);
        return toDomain(entity);
    }

    @Override
    public List<AnalysisResult> findAll() {
        return table.scan().items().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<AnalysisResult> findById(Long id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()))
            .map(this::toDomain);
    }

    private DynamoAnalysisResultEntity toEntity(AnalysisResult result) {
        DynamoAnalysisResultEntity entity = new DynamoAnalysisResultEntity();
        entity.setRepo(result.event().repo());
        entity.setBranch(result.event().branch());
        entity.setCategory(result.category());
        entity.setRootCause(result.rootCause());
        entity.setSummary(result.summary());
        return entity;
    }

    private AnalysisResult toDomain(DynamoAnalysisResultEntity entity) {
        BuildEvent event = new BuildEvent(null, entity.getRepo(), entity.getBranch(), null);
        return new AnalysisResult(
            entity.getId(), event, entity.getCategory(), entity.getRootCause(), entity.getSummary());
    }
}
