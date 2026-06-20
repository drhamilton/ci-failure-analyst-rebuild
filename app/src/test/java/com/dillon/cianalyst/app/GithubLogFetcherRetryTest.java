package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
class GithubLogFetcherRetryTest {

    // One server for the whole class, started before the Spring context so the
    // @DynamicPropertySource below can point the GitHub base-url at its port.
    static final MockWebServer server = new MockWebServer();

    static {
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void githubProps(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:" + server.getPort());
    }

    @Autowired
    GithubLogFetcher fetcher;

    private static final String JOBS_JSON = """
        {"jobs":[{"name":"build","conclusion":"failure",
                  "steps":[{"name":"compile","conclusion":"failure"}]}]}
        """;

    private static final BuildEvent EVENT =
        new BuildEvent("123", "owner/repo", "main", "failure");

    @Test
    void retriesTransientServerErrorsThenSucceeds() {
        server.enqueue(new MockResponse().setResponseCode(503));
        server.enqueue(new MockResponse().setResponseCode(503));
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(JOBS_JSON));

        int before = server.getRequestCount();

        BuildLog log = fetcher.fetch(EVENT);

        assertThat(log.content()).contains("build");
        assertThat(server.getRequestCount() - before).isEqualTo(3); // 2 failures + 1 success
    }
}
