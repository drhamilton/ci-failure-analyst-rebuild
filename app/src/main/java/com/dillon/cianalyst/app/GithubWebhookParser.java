package com.dillon.cianalyst.app;

import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.WebhookParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GithubWebhookParser implements WebhookParser {
    private final ObjectMapper mapper;

    @Override
    public boolean supports(String provider) {
        return "github".equals(provider);
    }
    
    @Override
    public BuildEvent parse(String rawPaylod) {
        try {
           JsonNode root = mapper.readTree(rawPaylod);
           JsonNode run = root.path("workflow_run");

           return new BuildEvent(
               run.path("id").asText(),
               root.path("repository").path("full_name").asText(),
               run.path("head_branch").asText(),
               run.path("conclusion").asText());
        } catch (JsonProcessingException e) {
            throw new WebhookParseException("Failed to parse webhook payload", e);
        }
    }
}