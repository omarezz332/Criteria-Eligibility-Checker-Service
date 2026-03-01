package com.eligibility.application.port.in;

import com.eligibility.domain.model.ApplicationPreference;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Applicant submits their ordered Preference List.
 * <p>
 * Triggered by: POST /api/applications/preferences
 * Implemented by: PreferenceService
 * <p>
 * This is the TRANSACTIONAL use case — all preferences are saved atomically.
 * If any single preference fails validation, nothing is saved.
 * <p>
 * Flow:
 * 1. Load applicant profile
 * 2. RE-VALIDATE eligibility server-side for every lottery in the list
 * 3. Recalculate rank mark (stored with each preference for historical accuracy)
 * 4. Delete applicant's existing preferences (resubmission replaces, not appends)
 * 5. Save all new preferences in one transaction
 * 6. Fire async email notification
 * <p>
 * Throws:
 * - ApplicantNotFoundException        if applicantId doesn't exist
 * - LotteryNotFoundException          if any lotteryId in preferences doesn't exist
 * - IneligibleLotteryException        if applicant is not eligible for a submitted lottery
 * - DuplicatePreferenceException      if same lottery or same order number appears twice
 * - ConcurrentPreferenceUpdateException if optimistic lock conflict detected
 */
public interface SubmitPreferenceUseCase {

    List<ApplicationPreference> submitPreferences(SubmitPreferenceCommand command);

    record SubmitPreferenceCommand(
            UUID applicantId,
            List<PreferenceEntry> preferences
    ) {
    }

    /**
     * A single entry in the submitted preference list.
     * <p>
     * preferenceOrderNum: applicant-defined priority (1 = first choice, 2 = second, ...)
     * Must be unique within the submission — no two entries can share the same order number.
     */
    record PreferenceEntry(
            UUID lotteryId,
            int preferenceOrderNum
    ) {
    }
}