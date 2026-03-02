package com.eligibility.application.dto.response;

import java.util.UUID;

public record CriteriaResponse(
        UUID id,
        String criteriaType,
        String criteriaValue
) {}
