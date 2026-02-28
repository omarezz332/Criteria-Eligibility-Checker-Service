package com.eligibility.domain.enums;

/**
 * All supported criteria types that can be configured per Lottery.
 * <p>
 * The Eligibility Engine uses these types to know how to evaluate
 * a criteria row against the applicant's profile.
 * <p>
 * How to add a new criteria:
 * 1. Add the enum value here
 * 2. Handle it in EligibilityEngine (application layer)
 * That's it — no schema changes needed (EAV pattern).
 */
public enum CriteriaType {

    /**
     * Applicant's age must be >= this value
     */
    AGE_MIN,

    /**
     * Applicant's age must be <= this value
     */
    AGE_MAX,

    /**
     * Applicant's gender must match exactly
     */
    GENDER,

    /**
     * Applicant's country must match exactly
     */
    COUNTRY,

    /**
     * Applicant's nationality must match exactly
     */
    NATIONALITY,

    /**
     * Applicant's marital status must match exactly
     */
    MARITAL_STATUS,

    /**
     * Whether applicant must have a disability (true/false)
     */
    HAS_DISABILITY,

    /**
     * Applicant's calculated RankMark must be >= this value
     */
    MIN_RANK_MARK
}