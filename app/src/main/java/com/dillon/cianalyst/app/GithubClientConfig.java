package com.dillon.cianalyst.app;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubClientConfig {

    @Bean
    RestClient githubRestClient(
        RestClient.Builder builder,
        @Value("${github.api.base-url}") String baseUrl,
        @Value("${github.token}") String token) {

            // Pin HTTP/1.1: the JDK client defaults to HTTP/2 and, over cleartext,
            // uses HTTP/2 prior-knowledge — which a plain HTTP/1.1 server can't parse.
            // Connect timeout lives on the HttpClient; read timeout on the factory.
            HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(200))
                .build();

            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Duration.ofMillis(2000));

            RestClient.Builder configured = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-Github-Api-Version", "2022-11-28")
                .requestFactory(requestFactory);

            if (StringUtils.hasText(token)) {
                configured = configured.defaultHeader("Authorization", "Bearer " + token);
            }

            return configured.build();
        }
}
