package com.eligibility.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * HTTP response for POST /api/applications/preferences
 * and GET /api/applications/preferences/{applicantId}
 */
public record PreferenceResponse(
        UUID applicantId,
        List<PreferenceEntry> preferences,
        String message
) {
    public record PreferenceEntry(
            UUID id,
            UUID lotteryId,
            double rankMark,
            int preferenceOrderNum,
            LocalDateTime createdDate
    ) {
    }
}