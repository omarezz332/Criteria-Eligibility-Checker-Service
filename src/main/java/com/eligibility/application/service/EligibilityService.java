package com.eligibility.application.service;

import com.eligibility.application.port.in.CheckEligibilityUseCase;

import java.util.UUID;

public class EligibilityService implements CheckEligibilityUseCase {

    @Override
    public EligibilityResult checkEligibility(UUID applicantId) {
        return null;
    }
}
