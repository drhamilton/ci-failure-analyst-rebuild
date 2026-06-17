package com.dillon.cianalyst.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.BuildEvent;

@ExtendWith(MockitoExtension.class)
public class DatastoreAnalysisResultStoreTest {
   
    @Mock AnalysisResultRepository repository;
    @Mock OutboxRepository outboxRepository;
    @InjectMocks DatastoreAnalysisResultStore store;

    @Test
    void savesByMappingDomainToEntity() {
        AnalysisResult result = new AnalysisResult(
            null,
            new BuildEvent(null, "dillon/payments-service", "main", null),
            "TEST_FAILURE",
            "NPE at PaymentService.java:42",
            "A test failed.");

        when(repository.save(any(AnalysisResultEntity.class)))
            .thenAnswer(returnsFirstArg());

        store.save(result);

        ArgumentCaptor<AnalysisResultEntity> captor = ArgumentCaptor.forClass(AnalysisResultEntity.class);

        verify(repository).save(captor.capture());
        AnalysisResultEntity persisted = captor.getValue();
        assertThat(persisted.repo).isEqualTo("dillon/payments-service");
        assertThat(persisted.branch).isEqualTo("main");
        assertThat(persisted.category).isEqualTo("TEST_FAILURE");
        assertThat(persisted.summary).isEqualTo("A test failed.");
    }

    @Test
    void findAllMapsEntitiesToDomain() {
        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.repo = "dillon/payments-service";
        entity.branch = "main";
        entity.summary = "A test failed.";
        when(repository.findAll()).thenReturn(List.of(entity));

        List<AnalysisResult> results = store.findAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).event().repo()).isEqualTo("dillon/payments-service");
        assertThat(results.get(0).summary()).isEqualTo("A test failed.");
    }
}
