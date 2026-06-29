package com.dillon.cianalyst.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain exceptions into clean HTTP responses (RFC-7807 ProblemDetail),
 * so internal failures don't leak as stack traces. Messages are deliberately generic.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WebhookVerificationException.class)
    ProblemDetail handleBadSignature(WebhookVerificationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid webhook signature");
    }

    @ExceptionHandler(UnsupportedProviderException.class)
    ProblemDetail handleUnsupportedProvider(UnsupportedProviderException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Unknown webhook provider");
    }
}
