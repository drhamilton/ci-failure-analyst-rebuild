package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Exercises {@link S3LogArchive} against a real S3 API served by LocalStack. No Spring
 * context is loaded — the adapter is wired by hand so the test stays isolated from the
 * (still GCP-bound) application autoconfiguration.
 */
@Testcontainers
class S3LogArchiveIntegrationTest {

    private static final String BUCKET = "test-logs";

    @Container
    static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4"))
        .withServices(LocalStackContainer.Service.S3);

    static S3LogArchive archive;

    @BeforeAll
    static void setUp() {
        S3Client s3 = S3Client.builder()
            .endpointOverride(URI.create(LOCALSTACK.getEndpoint().toString()))
            .region(Region.of(LOCALSTACK.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())))
            .httpClient(UrlConnectionHttpClient.create())
            .forcePathStyle(true)
            .build();

        s3.createBucket(b -> b.bucket(BUCKET));

        archive = new S3LogArchive(s3, BUCKET);
    }

    @Test
    void putStoresUnderDerivedKeyAndGetRoundTripsTheContent() {
        BuildEvent event = new BuildEvent("123", "drhamilton/ci-failure-analyst-rebuild", "main", "failure");
        BuildLog log = new BuildLog(event, "boom: tests failed");

        String key = archive.put(log);

        assertThat(key).isEqualTo("logs/drhamilton/ci-failure-analyst-rebuild/123.txt");
        assertThat(archive.get(key)).contains("boom: tests failed");
    }

    @Test
    void getReturnsEmptyForUnknownKey() {
        assertThat(archive.get("logs/does/not-exist.txt")).isEmpty();
    }
}
