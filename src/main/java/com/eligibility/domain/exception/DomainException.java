package com.eligibility.domain.exception;

/**
 * Base exception for all domain-level errors.
 *
 * All domain exceptions extend this so that callers (controllers, services)
 * can catch domain errors generically if needed, or handle specific subtypes.
 *
 * These exceptions carry no HTTP concepts — the presentation layer
 * is responsible for mapping them to HTTP status codes.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}