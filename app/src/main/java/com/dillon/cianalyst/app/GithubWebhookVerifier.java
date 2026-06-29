package com.dillon.cianalyst.app;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Verifies that a webhook payload genuinely came from GitHub, by recomputing the
 * HMAC-SHA256 of the raw body with our shared secret and comparing it to the
 * signature GitHub sent in the X-Hub-Signature-256 header.
 */
@Component
public class GithubWebhookVerifier {
    private static final String ALGORITHM = "HmacSHA256";

    private final String secret;

    public GithubWebhookVerifier(@Value("${github.webhook.secret}") String secret) {
        this.secret = secret;
    }

    /** Throws {@link WebhookVerificationException} if the signature is missing or wrong. */
    public void verify(String payload, String signatureHeader) {
        if (!StringUtils.hasText(signatureHeader)) {
            throw new WebhookVerificationException("Missing webhook signature");
        }

        String expected = "sha256=" + hmacHex(payload);

        // Constant-time comparison: a plain String.equals would leak, via timing,
        // how many leading characters matched.
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = signatureHeader.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
            throw new WebhookVerificationException("Webhook signature mismatch");
        }
    }

    private String hmacHex(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new WebhookVerificationException("Failed to compute HMAC", e);
        }
    }
}
