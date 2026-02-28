package com.eligibility.domain.exception;

import java.util.UUID;

/**
 * Thrown when an applicant attempts to add a lottery to their preference list
 * that they are NOT eligible for.
 *
 * This is a server-side re-validation guard — we never trust the client
 * to only send eligible lotteries. We always re-check before saving.
 *
 * Maps to HTTP 422 Unprocessable Entity in the presentation layer.
 */
public class IneligibleLotteryException extends DomainException {

    private static final String ERROR_CODE = "LOTTERY_NOT_ELIGIBLE";

    public IneligibleLotteryException(UUID applicantId, UUID lotteryId) {
        super(
                ERROR_CODE,
                "Applicant [" + applicantId + "] is not eligible for Lottery [" + lotteryId + "]. " +
                        "Only lotteries returned by the eligibility check can be added to preferences."
        );
    }
}