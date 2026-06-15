package com.dillon.cianalyst.app;

public class UnsupportedProviderException extends RuntimeException {
    public UnsupportedProviderException(String provider) {
        super("No parser supports provider: " + provider);
    }
}
