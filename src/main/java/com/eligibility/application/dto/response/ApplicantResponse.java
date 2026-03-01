package com.eligibility.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HTTP response for POST /api/applicants
 * <p>
 * Returns the registered applicant with their generated ID
 * and calculated rank mark — so the applicant knows their score
 * immediately after registration.
 */
public record ApplicantResponse(
        UUID id,
        String name,
        int age,
        String gender,
        String country,
        String nationality,
        String maritalStatus,
        boolean hasDisability,
        String disabilityName,
        String educationLevel,
        int careerYears,
        double rankMark,          // calculated — not stored in DB, derived here
        LocalDateTime createdDate
) {
}