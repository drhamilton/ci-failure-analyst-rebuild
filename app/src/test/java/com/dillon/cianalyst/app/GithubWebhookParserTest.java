package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.dillon.cianalyst.core.BuildEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

class GithubWebhookParserTest {
    @Test
    void parsesWorkflowRunFields() {
        String payload = """
            {
              "action": "completed",
              "workflow_run": {
                "id": 9876543210,
                "head_branch": "main",
                "status": "completed",
                "conclusion": "failure"
              },
              "repository": { "full_name": "dillon/payments-service" }
            }
            """;

        BuildEvent event = new GithubWebhookParser(new ObjectMapper()).parse(payload);

        assertThat(event.id()).isEqualTo("9876543210");
        assertThat(event.repo()).isEqualTo("dillon/payments-service");
        assertThat(event.branch()).isEqualTo("main");
        assertThat(event.status()).isEqualTo("failure");
    }

    @Test
    void throwsOnMalformedJson() {
        GithubWebhookParser parser = new GithubWebhookParser(new ObjectMapper());

        assertThatThrownBy(() -> parser.parse("This is not json {{{"))
            .isInstanceOf(WebhookParseException.class)
            .hasMessageContaining("Failed to parse webhook payload");
    }
}