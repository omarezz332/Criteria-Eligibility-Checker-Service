package com.eligibility.presentation;

import com.eligibility.application.dto.response.ErrorResponse;
import com.eligibility.domain.exception.ApplicantNotFoundException;
import com.eligibility.domain.exception.ConcurrentPreferenceUpdateException;
import com.eligibility.domain.exception.DomainException;
import com.eligibility.domain.exception.DuplicatePreferenceException;
import com.eligibility.domain.exception.IneligibleLotteryException;
import com.eligibility.domain.exception.LotteryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler — the ONLY place that maps exceptions to HTTP responses.
 *
 * This is why controllers have no try/catch blocks.
 * This is why domain exceptions carry no HTTP concepts.
 *
 * Every exception thrown anywhere in the application bubbles up here,
 * gets mapped to a clean ErrorResponse, and returned to the client.
 *
 * Mapping:
 *   ApplicantNotFoundException          → 404 Not Found
 *   LotteryNotFoundException            → 404 Not Found
 *   IneligibleLotteryException          → 422 Unprocessable Entity
 *   DuplicatePreferenceException        → 409 Conflict
 *   ConcurrentPreferenceUpdateException → 409 Conflict
 *   IllegalArgumentException            → 400 Bad Request
 *   MethodArgumentTypeMismatchException → 400 Bad Request (invalid path/query param type)
 *   MethodArgumentNotValidException     → 400 Bad Request (field validation)
 *   Exception (fallback)                → 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 404 — Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler({ApplicantNotFoundException.class, LotteryNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(DomainException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, ex.getErrorCode(), ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 409 — Conflict (duplicate or concurrent update)
    // -------------------------------------------------------------------------

    @ExceptionHandler({DuplicatePreferenceException.class, ConcurrentPreferenceUpdateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(DomainException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, ex.getErrorCode(), ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 422 — Unprocessable Entity (business rule violation)
    // -------------------------------------------------------------------------

    @ExceptionHandler(IneligibleLotteryException.class)
    public ResponseEntity<ErrorResponse> handleIneligible(IneligibleLotteryException ex) {
        log.warn("Eligibility violation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, ex.getErrorCode(), ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 400 — Bad Request (invalid input from service layer)
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "INVALID_INPUT", ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 400 — Type mismatch (e.g. invalid UUID in path/query param)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        log.warn("Type mismatch: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "INVALID_PARAMETER", message));
    }

    // -------------------------------------------------------------------------
    // 400 — Validation errors (@Valid annotation failures)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(error.getField() + ": " + error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofValidation(fieldErrors));
    }

    // -------------------------------------------------------------------------
    // 500 — Fallback for anything unexpected
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later."));
    }
}