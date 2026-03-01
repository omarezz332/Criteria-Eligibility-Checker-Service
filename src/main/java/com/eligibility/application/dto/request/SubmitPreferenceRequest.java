package com.eligibility.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * HTTP request body for POST /api/applications/preferences
 *
 * Example body:
 * {
 *   "preferences": [
 *     { "lotteryId": "uuid-1", "preferenceOrderNum": 1 },
 *     { "lotteryId": "uuid-2", "preferenceOrderNum": 2 }
 *   ]
 * }
 */
public record SubmitPreferenceRequest(

        @NotEmpty(message = "preferences must not be empty")
        @Valid
        List<PreferenceEntryRequest> preferences
) {
    public record PreferenceEntryRequest(

            @NotNull(message = "lotteryId is required")
            UUID lotteryId,

            @NotNull(message = "preferenceOrderNum is required")
            @Min(value = 1, message = "preferenceOrderNum must be >= 1")
            Integer preferenceOrderNum
    ) {}
}