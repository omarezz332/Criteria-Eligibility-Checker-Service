package com.eligibility.application.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * HTTP response for POST /api/applications/eligibility
 * <p>
 * Tells the applicant:
 * - Their calculated rank mark
 * - Which lotteries they qualify for
 * - A human-readable message
 * <p>
 * Example response when eligible:
 * {
 * "applicantId": "uuid",
 * "rankMark": 62.0,
 * "eligibleLotteries": [
 * { "id": "uuid-1", "name": "Housing Lottery", ... }
 * ],
 * "message": "You are eligible for 1 lottery."
 * }
 * <p>
 * Example response when not eligible for anything:
 * {
 * "applicantId": "uuid",
 * "rankMark": 20.0,
 * "eligibleLotteries": [],
 * "message": "No eligible lotteries found for your profile."
 * }
 */
public record EligibilityResponse(
        UUID applicantId,
        double rankMark,
        List<LotteryResponse> eligibleLotteries,
        String message
) {
}