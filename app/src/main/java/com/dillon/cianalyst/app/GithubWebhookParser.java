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

           BuildEvent event = new BuildEvent();

           event.id = run.path("id").asText();
           event.repo = root.path("repository").path("full_name").asText();
           event.branch = run.path("head_branch").asText();
           event.status = run.path("conclusion").asText();
           return event;
        } catch (JsonProcessingException e) {
            throw new WebhookParseException("Failed to parse webhook payload", e);
        }
    }
}