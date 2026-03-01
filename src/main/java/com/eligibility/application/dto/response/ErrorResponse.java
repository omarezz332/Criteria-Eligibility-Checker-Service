package com.eligibility.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uniform error response shape returned for all error cases.
 * <p>
 * Every error — validation failure, not found, conflict, server error —
 * returns this same structure so clients can handle errors consistently.
 * <p>
 * Example (validation error):
 * {
 * "status": 400,
 * "errorCode": "VALIDATION_ERROR",
 * "message": "Request validation failed",
 * "errors": ["age must be at least 18", "name must not be blank"],
 * "timestamp": "2024-01-15T10:30:00"
 * }
 * <p>
 * Example (not found):
 * {
 * "status": 404,
 * "errorCode": "APPLICANT_NOT_FOUND",
 * "message": "Applicant not found with id: uuid",
 * "errors": [],
 * "timestamp": "2024-01-15T10:30:00"
 * }
 */
public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        List<String> errors,         // field-level validation messages
        LocalDateTime timestamp
) {
    /**
     * Convenience factory for single-message errors
     */
    public static ErrorResponse of(int status, String errorCode, String message) {
        return new ErrorResponse(status, errorCode, message, List.of(), LocalDateTime.now());
    }

    /**
     * Convenience factory for validation errors with multiple field messages
     */
    public static ErrorResponse ofValidation(List<String> fieldErrors) {
        return new ErrorResponse(
                400, "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors,
                LocalDateTime.now()
        );
    }
}