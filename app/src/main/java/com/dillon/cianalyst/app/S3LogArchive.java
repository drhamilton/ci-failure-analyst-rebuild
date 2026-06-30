package com.dillon.cianalyst.app;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.LogArchive;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * S3-backed {@link LogArchive}, active under the {@code aws} profile. Stashes the raw
 * build log as an object so DynamoDB only has to keep the (small) key, not the (large)
 * log body. The {@link S3Client} is wired by {@link S3Config}.
 */
@Component
@Profile("aws")
public class S3LogArchive implements LogArchive {

    private final S3Client s3;
    private final String bucket;

    public S3LogArchive(S3Client s3, @Value("${aws.s3.bucket:ci-failure-analyst-logs}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String put(BuildLog log) {
        String key = keyFor(log);
        s3.putObject(b -> b.bucket(bucket).key(key), RequestBody.fromString(log.content()));
        return key;
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.of(s3.getObjectAsBytes(b -> b.bucket(bucket).key(key)).asUtf8String());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }
}
