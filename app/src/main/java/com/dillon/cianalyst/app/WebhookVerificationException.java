package com.dillon.cianalyst.app;

/** Thrown when a webhook's signature is missing or does not match the expected HMAC. */
public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message) {
        super(message);
    }

    public WebhookVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
