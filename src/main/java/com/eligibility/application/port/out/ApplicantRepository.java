package com.eligibility.application.port.out;

import com.eligibility.domain.model.ApplicantProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: Applicant persistence contract.
 * <p>
 * Implemented by: ApplicantRepositoryAdapter (infrastructure layer)
 * <p>
 * The application layer depends on this interface — it never imports
 * any JPA class, Spring Data class, or SQL directly.
 * That dependency direction is what Clean Architecture enforces.
 */
public interface ApplicantRepository {

    /**
     * Persist a new applicant profile.
     * Returns the saved profile with its generated ID populated.
     */
    ApplicantProfile save(ApplicantProfile applicant);

    /**
     * Find an applicant by their ID.
     * Returns Optional.empty() if not found — never throws here.
     * The service layer decides whether to throw ApplicantNotFoundException.
     */
    Optional<ApplicantProfile> findById(UUID id);

    /**
     * Check if an applicant exists without loading the full profile.
     * Cheaper than findById when you only need existence confirmation.
     */
    boolean existsById(UUID id);
}