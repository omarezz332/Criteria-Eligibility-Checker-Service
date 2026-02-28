package com.eligibility.domain.exception;

import java.util.UUID;

/**
 * Thrown when a lottery lookup by ID returns no result.
 * Maps to HTTP 404 Not Found in the presentation layer.
 */
public class LotteryNotFoundException extends DomainException {

    private static final String ERROR_CODE = "LOTTERY_NOT_FOUND";

    public LotteryNotFoundException(UUID lotteryId) {
        super(ERROR_CODE, "Lottery not found with id: " + lotteryId);
    }
}