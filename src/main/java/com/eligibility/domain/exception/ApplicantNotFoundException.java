package com.eligibility.domain.exception;

import java.util.UUID;

/**
 * Thrown when an applicant lookup by ID returns no result.
 * Maps to HTTP 404 Not Found in the presentation layer.
 */
public class ApplicantNotFoundException extends DomainException {

    private static final String ERROR_CODE = "APPLICANT_NOT_FOUND";

    public ApplicantNotFoundException(UUID applicantId) {
        super(ERROR_CODE, "Applicant not found with id: " + applicantId);
    }
}