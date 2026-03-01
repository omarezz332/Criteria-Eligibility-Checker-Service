package com.eligibility.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * HTTP request body for POST /api/admin/lotteries/{id}/criteria
 *
 * Accepts a batch of criteria — admin configures all rules
 * for a lottery in one request.
 *
 * Example body:
 * {
 *   "criteria": [
 *     { "criteriaType": "AGE_MIN",       "criteriaValue": "25"      },
 *     { "criteriaType": "GENDER",        "criteriaValue": "MALE"    },
 *     { "criteriaType": "MIN_RANK_MARK", "criteriaValue": "40"      }
 *   ]
 * }
 */
public record AddCriteriaRequest(

        @NotEmpty(message = "criteria list must not be empty")
        @Valid
        List<CriteriaEntry> criteria
) {
    public record CriteriaEntry(

            @NotBlank(message = "criteriaType must not be blank")
            String criteriaType,    // maps to CriteriaType enum

            @NotBlank(message = "criteriaValue must not be blank")
            String criteriaValue
    ) {}
}