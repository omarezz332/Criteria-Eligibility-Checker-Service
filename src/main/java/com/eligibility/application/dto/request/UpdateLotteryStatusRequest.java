package com.eligibility.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * HTTP request body for PATCH /api/admin/lotteries/{id}/status
 *
 * Example body:
 * { "status": "NOT_ACTIVE" }
 */
public record UpdateLotteryStatusRequest(

        @NotBlank(message = "status must not be blank")
        String status       // "ACTIVE" | "NOT_ACTIVE"
) {}