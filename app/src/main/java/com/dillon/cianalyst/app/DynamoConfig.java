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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Wires the DynamoDB clients under the {@code aws} profile. When
 * {@code aws.dynamodb.endpoint} is set (LocalStack/local dev) it overrides the
 * endpoint and uses dummy static credentials; otherwise it falls back to the
 * default credentials chain — which on ECS resolves to the task role, no keys.
 */
@Configuration
@Profile("aws")
public class DynamoConfig {

    @Bean
    DynamoDbClient dynamoDbClient(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.dynamodb.endpoint:}") String endpoint) {
        var builder = DynamoDbClient.builder()
            .region(Region.of(region))
            .httpClient(UrlConnectionHttpClient.create());

        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("test", "test")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    @Bean
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Bean
    DynamoDbTable<DynamoAnalysisResultEntity> analysisResultTable(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name:analysis_results}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(DynamoAnalysisResultEntity.class));
    }
}
