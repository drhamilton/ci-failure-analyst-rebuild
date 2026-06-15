package com.dillon.cianalyst.app;

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

    @Test
    void delegatesToService() throws Exception {
        String payload = "{\"hello\":\"world\"}";

        mockMvc.perform(post("/webhook/buildkite")
            .contentType("application/json")
            .content(payload))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));

        verify(service).analyze("buildkite", payload);
    }
}
