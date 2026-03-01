package com.eligibility.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HTTP response for lottery operations.
 * Used in admin endpoints and inside EligibilityResponse.
 */
public record LotteryResponse(
        UUID id,
        String name,
        String status,
        LocalDateTime createdDate
) {
}