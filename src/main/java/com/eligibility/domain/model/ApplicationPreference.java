package com.eligibility.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single entry in an applicant's preference list.
 */
public record ApplicationPreference(UUID id, UUID applicantId, UUID lotteryId, double lotteryRankMark,
                                    int preferenceOrderNum, LocalDateTime createdDate) {

    // -------------------------------------------------------------------------
    // Validation (domain guards called before saving)
    // -------------------------------------------------------------------------

    public static void validatePreferenceOrderNum(int num) {
        if (num < 1) {
            throw new IllegalArgumentException(
                    "Preference order number must be >= 1, got: " + num
            );
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ApplicationPreference{" +
                "applicantId=" + applicantId +
                ", lotteryId=" + lotteryId +
                ", order=" + preferenceOrderNum +
                ", rankMark=" + lotteryRankMark +
                '}';
    }
}