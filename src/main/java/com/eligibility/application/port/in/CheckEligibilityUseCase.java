package com.eligibility.application.port.in;

import com.eligibility.domain.model.Lottery;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Check which active Lotteries an Applicant is eligible for.
 * <p>
 * Triggered by: POST /api/applications/eligibility
 * Implemented by: EligibilityService
 * <p>
 * This is the core use case of the system.
 * <p>
 * Flow:
 * 1. Load applicant profile
 * 2. Calculate applicant's RankMark
 * 3. Load all ACTIVE lotteries with their criteria (from cache)
 * 4. Run each lottery's criteria list against the applicant's profile
 * 5. Return only the lotteries where ALL criteria pass
 * <p>
 * Returns an empty list (not an error) if no lotteries match — valid outcome.
 * <p>
 * Throws ApplicantNotFoundException if applicantId does not exist.
 */
public interface CheckEligibilityUseCase {

    EligibilityResult checkEligibility(UUID applicantId);

    /**
     * Result object returned to the applicant.
     * <p>
     * Contains:
     * - The applicant's calculated rank mark (so they can see it)
     * - The list of lotteries they are eligible for
     */
    record EligibilityResult(
            UUID applicantId,
            double rankMark,
            List<Lottery> eligibleLotteries
    ) {
        public boolean hasEligibleLotteries() {
            return eligibleLotteries != null && !eligibleLotteries.isEmpty();
        }
    }
}