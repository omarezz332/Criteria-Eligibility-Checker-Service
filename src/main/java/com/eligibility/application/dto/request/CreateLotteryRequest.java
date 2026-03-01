package com.eligibility.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * HTTP request body for POST /api/admin/lotteries
 */
public record CreateLotteryRequest(

        @NotBlank(message = "name must not be blank")
        String name
) {}