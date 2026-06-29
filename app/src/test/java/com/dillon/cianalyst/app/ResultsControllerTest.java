package com.dillon.cianalyst.app;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.dillon.cianalyst.core.AnalysisResultStore;

@WebMvcTest(ResultsController.class)
@Import(SecurityConfig.class)
public class ResultsControllerTest {
    @Autowired MockMvc mockMvc;

    @MockBean AnalysisResultStore store;

    @Test
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/results"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAccessWithValidCredentials() throws Exception {
        when(store.findAll()).thenReturn(List.of());

        // HTTP Basic = base64("user:password") — encoded, not encrypted (needs HTTPS).
        String creds = Base64.getEncoder().encodeToString("testuser:testpass".getBytes());

        mockMvc.perform(get("/results").header("Authorization", "Basic " + creds))
            .andExpect(status().isOk());
    }
}
