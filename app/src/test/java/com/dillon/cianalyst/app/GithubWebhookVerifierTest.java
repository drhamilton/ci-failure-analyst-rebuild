package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GithubWebhookVerifierTest {
    private static final String SECRET = "It's a Secret to Everybody";
    private static final String PAYLOAD = "Hello, World!";
    private static final String VALID = "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

    private final GithubWebhookVerifier verifier = new GithubWebhookVerifier(SECRET);

    @Test
    void acceptsValidSignature() {
        assertThatNoException().isThrownBy(() -> verifier.verify(PAYLOAD, VALID));
    }

    @Test
    void rejectsWrongSignature() {
        assertThatThrownBy(() -> verifier.verify(PAYLOAD, "sha256=deadbeef"))
            .isInstanceOf(WebhookVerificationException.class);
    }

    @Test
    void rejectsMissingSignature() {
        assertThatThrownBy(() -> verifier.verify(PAYLOAD, null))
            .isInstanceOf(WebhookVerificationException.class);
    }
}