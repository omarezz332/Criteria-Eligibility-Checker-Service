package com.eligibility.domain.exception;

import java.util.UUID;

/**
 * Thrown when an applicant tries to submit the same lottery twice
 * in their preference list, or uses the same preference order number twice.
 *
 * Maps to HTTP 409 Conflict in the presentation layer.
 */
public class DuplicatePreferenceException extends DomainException {

    private static final String ERROR_CODE = "DUPLICATE_PREFERENCE";

    public DuplicatePreferenceException(UUID applicantId, UUID lotteryId) {
        super(
                ERROR_CODE,
                "Applicant [" + applicantId + "] already has Lottery [" + lotteryId + "] in their preference list."
        );
    }

    public DuplicatePreferenceException(UUID applicantId, int preferenceOrderNum) {
        super(
                ERROR_CODE,
                "Applicant [" + applicantId + "] already has a preference with order number [" + preferenceOrderNum + "]."
        );
    }
}