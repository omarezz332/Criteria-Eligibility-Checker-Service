package com.eligibility.application.port.in;

import com.eligibility.domain.model.ApplicantProfile;

/**
 * Use Case: Register a new applicant profile in the system.
 * <p>
 * Triggered by: POST /api/applicants
 * Implemented by: ApplicantService
 * <p>
 * Command carries all registration data.
 * Returns the created ApplicantProfile (with generated ID).
 */
public interface RegisterApplicantUseCase {

    ApplicantProfile register(RegisterApplicantCommand command);

    /**
     * Immutable command object — all data needed to register an applicant.
     * Using a nested record keeps the command co-located with its use case.
     */
    record RegisterApplicantCommand(
            String name,
            int age,
            String gender,
            String country,
            String nationality,
            String maritalStatus,
            boolean hasDisability,
            String disabilityName,
            String educationLevel,
            int careerYears
    ) {
    }
}