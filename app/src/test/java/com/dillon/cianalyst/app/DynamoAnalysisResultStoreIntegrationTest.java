package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.BuildEvent;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Exercises {@link DynamoAnalysisResultStore} against a real DynamoDB API served by
 * LocalStack. No Spring context is loaded — the adapter is wired by hand so the test
 * stays isolated from the (still GCP-bound) application autoconfiguration.
 */
@Testcontainers
class DynamoAnalysisResultStoreIntegrationTest {

    @Container
    static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4"))
        .withServices(LocalStackContainer.Service.DYNAMODB);

    static DynamoAnalysisResultStore store;

    @BeforeAll
    static void setUp() {
        DynamoDbClient client = DynamoDbClient.builder()
            .endpointOverride(URI.create(LOCALSTACK.getEndpoint().toString()))
            .region(Region.of(LOCALSTACK.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())))
            .httpClient(UrlConnectionHttpClient.create())
            .build();

        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(client)
            .build();

        DynamoDbTable<DynamoAnalysisResultEntity> table = enhanced.table(
            "analysis_results", TableSchema.fromBean(DynamoAnalysisResultEntity.class));
        table.createTable();
        client.waiter().waitUntilTableExists(b -> b.tableName("analysis_results"));

        store = new DynamoAnalysisResultStore(table);
    }

    @Test
    void savesAssignsAnIdAndRoundTripsById() {
        AnalysisResult saved = store.save(new AnalysisResult(
            null,
            new BuildEvent(null, "dillon/payments-service", "main", null),
            "TEST_FAILURE",
            "NPE at PaymentService.java:42",
            "A test failed.",
            "logs/dillon/payments-service/1.txt"));

        assertThat(saved.id()).isNotNull();

        Optional<AnalysisResult> found = store.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().event().repo()).isEqualTo("dillon/payments-service");
        assertThat(found.get().event().branch()).isEqualTo("main");
        assertThat(found.get().category()).isEqualTo("TEST_FAILURE");
        assertThat(found.get().rootCause()).isEqualTo("NPE at PaymentService.java:42");
        assertThat(found.get().summary()).isEqualTo("A test failed.");
        assertThat(found.get().logKey()).isEqualTo("logs/dillon/payments-service/1.txt");
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(store.findById(999999L)).isEmpty();
    }

    @Test
    void findAllReturnsSavedResults() {
        store.save(new AnalysisResult(
            null,
            new BuildEvent(null, "dillon/orders-service", "release", null),
            "COMPILE_ERROR",
            "missing semicolon",
            "Build broke.",
            null));

        assertThat(store.findAll())
            .extracting(r -> r.event().repo())
            .contains("dillon/orders-service");
    }
}
