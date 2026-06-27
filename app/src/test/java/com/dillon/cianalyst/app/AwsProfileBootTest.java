package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dillon.cianalyst.core.AnalysisResultStore;

/**
 * Verifies the application context boots under the {@code aws} profile — i.e. the GCP
 * Datastore/context autoconfig is excluded (no GCP credentials or emulator needed) and
 * the DynamoDB adapter is wired in its place. The DynamoDB clients build lazily, so this
 * needs no running DynamoDB / Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("aws")
class AwsProfileBootTest {

    @Autowired
    AnalysisResultStore store;

    @Test
    void bootsWithDynamoStoreUnderAwsProfile() {
        assertThat(store).isInstanceOf(DynamoAnalysisResultStore.class);
    }
}
