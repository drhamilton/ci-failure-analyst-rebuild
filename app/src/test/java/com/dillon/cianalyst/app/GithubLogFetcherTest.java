package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;

class GithubLogFetcherTest {

    MockRestServiceServer server;
    GithubLogFetcher fetcher;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.github.com");
        server = MockRestServiceServer.bindTo(builder).build();
        fetcher = new GithubLogFetcher(builder.build());
    }

    @Test
    void fetchesJobsAndSummarizesFailures() throws Exception {
        String body = new ClassPathResource("github/jobs-response.json")
            .getContentAsString(StandardCharsets.UTF_8);

        server.expect(requestTo(
                "https://api.github.com/repos/spring-projects/spring-boot/actions/runs/27768804651/jobs"))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        BuildEvent event =
            new BuildEvent("27768804651", "spring-projects/spring-boot", "main", "failure");

        BuildLog log = fetcher.fetch(event);

        assertThat(log.content()).contains("Windows | Java 21");
        assertThat(log.content()).contains("Build");
        server.verify();
    }
}
