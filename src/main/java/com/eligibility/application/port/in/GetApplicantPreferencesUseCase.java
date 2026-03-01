package com.eligibility.application.port.in;

import com.eligibility.domain.model.ApplicationPreference;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Retrieve the current preference list for an applicant.
 * <p>
 * Triggered by: GET /api/applications/preferences/{applicantId}
 * Implemented by: PreferenceService
 * <p>
 * Returns preferences ordered by preferenceOrderNum ascending.
 * Returns empty list if applicant has not submitted any preferences yet.
 * <p>
 * Throws ApplicantNotFoundException if applicantId does not exist.
 */
public interface GetApplicantPreferencesUseCase {

    List<ApplicationPreference> getPreferences(UUID applicantId);
}