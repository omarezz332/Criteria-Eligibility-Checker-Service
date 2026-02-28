package com.eligibility.domain.exception;

import java.util.UUID;

/**
 * Thrown when two concurrent requests attempt to update the same applicant's
 * preference list at the same time and an optimistic lock conflict is detected.
 *
 * The caller (controller) should instruct the client to retry the request.
 *
 * Maps to HTTP 409 Conflict in the presentation layer.
 */
public class ConcurrentPreferenceUpdateException extends DomainException {

    private static final String ERROR_CODE = "CONCURRENT_UPDATE_CONFLICT";

    public ConcurrentPreferenceUpdateException(UUID applicantId) {
        super(
                ERROR_CODE,
                "Concurrent update detected for applicant [" + applicantId + "]. " +
                        "Please retry your request."
        );
    }
}