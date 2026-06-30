package com.dillon.cianalyst.app;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Wires the S3 client under the {@code aws} profile. When {@code aws.s3.endpoint} is set
 * (LocalStack/local dev) it overrides the endpoint, forces path-style access, and uses
 * dummy static credentials; otherwise it falls back to the default credentials chain —
 * which on ECS resolves to the task role, no keys.
 */
@Configuration
@Profile("aws")
public class S3Config {

    @Bean
    S3Client s3Client(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.s3.endpoint:}") String endpoint) {
        var builder = S3Client.builder()
            .region(Region.of(region))
            .httpClient(UrlConnectionHttpClient.create());

        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("test", "test")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}
