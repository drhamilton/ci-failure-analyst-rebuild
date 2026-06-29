package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebhookController.class)
public class WebhookControllerTest {
    @Autowired MockMvc mockMvc;

    @MockBean FailureAnalysisService service;
    @MockBean GithubWebhookVerifier verifier;

    @Test
    void delegatesToServiceWhenSignatureValid() throws Exception {
        // verifier is mocked → verify() does nothing → signature treated as valid
        String payload = "{\"hello\":\"world\"}";

        mockMvc.perform(post("/webhook/github")
            .contentType("application/json")
            .header("X-Hub-Signature-256", "sha256=whatever")
            .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));

        verify(service).analyze("github", payload);
    }

    @Test
    void doesNotAnalyzeWhenSignatureInvalid() throws Exception {
        doThrow(new WebhookVerificationException("bad signature"))
            .when(verifier).verify(any(), any());

        assertThatThrownBy(() ->
            mockMvc.perform(post("/webhook/github")
                .contentType("application/json")
                .header("X-Hub-Signature-256", "sha256=bad")
                .content("{}")))
            .hasCauseInstanceOf(WebhookVerificationException.class);

        // The security invariant: a bad signature never reaches analysis.
        verify(service, never()).analyze(any(), any());
    }
}
