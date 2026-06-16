package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dillon.cianalyst.core.AnalysisResultStore;

@SpringBootTest
public class AnalysisResultRepositoryIntegrationTest {
    @Autowired AnalysisResultRepository repository;

    AnalysisResultStore store;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void savesAndReadsBack() {
        AnalysisResultEntity entity = new AnalysisResultEntity();

        entity.repo = "dillon/payments-service";
        entity.branch = "main";
        entity.category = "TEST_FAILURE";
        entity.rootCause = "NPE at PaymentService.java:42";
        entity.summary = "A test failed.";

        AnalysisResultEntity saved = repository.save(entity);
        assertThat(saved.id).isNotNull();

        Optional<AnalysisResultEntity> found = repository.findById(saved.id);
        assertThat(found).isPresent();
        assertThat(found.get().summary).isEqualTo("A test failed.");
    }
}
