package com.dillon.cianalyst.app;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubClientConfig {
    
    @Bean
    RestClient githubRestClient(
        RestClient.Builder builder,
        @Value("${github.api.base-url}") String baseUrl,
        @Value("${github.token}") String token) {
            RestClient.Builder configured = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-Github-Api-Version", "2022-11-28");

            if (StringUtils.hasText(token)) {
                configured = configured.defaultHeader("Authorization", "Bearer " + token);
            }

            return configured.build();
        }
}
