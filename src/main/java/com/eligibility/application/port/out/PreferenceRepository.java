package com.eligibility.application.port.out;

import com.eligibility.domain.model.ApplicationPreference;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port: Application preference persistence contract.
 * <p>
 * Implemented by: PreferenceRepositoryAdapter (infrastructure layer)
 * <p>
 * All write operations here participate in the caller's transaction.
 * The service layer controls the transaction boundary — not this port.
 */
public interface PreferenceRepository {

    /**
     * Save a batch of preferences atomically.
     * Called inside a transaction — if any row fails, all roll back.
     * Returns the saved preferences with generated IDs.
     */
    List<ApplicationPreference> saveAll(List<ApplicationPreference> preferences);

    /**
     * Load all preferences for an applicant, ordered by preferenceOrderNum ASC.
     */
    List<ApplicationPreference> findByApplicantIdOrderByPreferenceOrderNum(UUID applicantId);

    /**
     * Delete ALL existing preferences for an applicant.
     * <p>
     * Called before saving a new preference list — resubmission
     * always replaces the old list completely (not appends).
     * Must run inside the same transaction as saveAll.
     */
    void deleteAllByApplicantId(UUID applicantId);

    /**
     * Check if an applicant has already submitted preferences.
     * Used to decide between "first submission" vs "resubmission" flow.
     */
    boolean existsByApplicantId(UUID applicantId);
}