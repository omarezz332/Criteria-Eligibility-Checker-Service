package com.eligibility.application.service;


import com.eligibility.application.port.in.GetApplicantPreferencesUseCase;
import com.eligibility.application.port.in.SubmitPreferenceUseCase;
import com.eligibility.application.port.out.ApplicantRepository;
import com.eligibility.application.port.out.LotteryRepository;
import com.eligibility.application.port.out.PreferenceRepository;
import com.eligibility.domain.exception.ApplicantNotFoundException;
import com.eligibility.domain.exception.DuplicatePreferenceException;
import com.eligibility.domain.exception.IneligibleLotteryException;
import com.eligibility.domain.exception.LotteryNotFoundException;
import com.eligibility.domain.model.ApplicantProfile;
import com.eligibility.domain.model.ApplicationPreference;
import com.eligibility.domain.model.Lottery;
import com.eligibility.domain.service.EligibilityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service: handles preference list submission and retrieval.
 * <p>
 * Implements:
 * - SubmitPreferenceUseCase  (WRITE — transactional, concurrency-safe)
 * - GetApplicantPreferencesUseCase (READ — read-only transaction)
 */

@Service
public class PreferenceService implements SubmitPreferenceUseCase, GetApplicantPreferencesUseCase {
    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);
    private final ApplicantRepository applicantRepository;
    private final LotteryRepository lotteryRepository;
    private final EligibilityEngine eligibilityEngine;
    private final PreferenceRepository preferenceRepository;


    public PreferenceService(ApplicantRepository applicantRepository, LotteryRepository lotteryRepository, EligibilityEngine eligibilityEngine, PreferenceRepository preferenceRepository) {
        this.applicantRepository = applicantRepository;
        this.lotteryRepository = lotteryRepository;
        this.eligibilityEngine = eligibilityEngine;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ApplicationPreference> submitPreferences(SubmitPreferenceCommand command) {
        log.info("Processing preference submission for applicant: {}", command.applicantId());

        // ------------------------------------------------------------------
        // Step 1: Load applicant — throws if not found
        // ------------------------------------------------------------------
        ApplicantProfile applicant = applicantRepository.findById(command.applicantId())
                .orElseThrow(() -> new ApplicantNotFoundException(command.applicantId()));
        // ------------------------------------------------------------------
        // Step 2: Calculate rank mark once — used in re-validation + storage
        // ------------------------------------------------------------------
        double rankMark = applicant.calculateRankMark();
        // ------------------------------------------------------------------
        // Step 3: Validate the preference list structure (before hitting DB)
        // ------------------------------------------------------------------
        validatePreferenceListStructure(command);
        // ------------------------------------------------------------------
        // Step 4: Re-validate eligibility server-side for every lottery
        //         Load each lottery and check ALL its criteria
        // ------------------------------------------------------------------
        List<Lottery> resolvedLotteries = resolveAndValidateLotteries(
                command, applicant, rankMark
        );
        // ------------------------------------------------------------------
        // Step 5: Delete existing preferences for this applicant
        //         (resubmission replaces — does not append)
        // ------------------------------------------------------------------
        preferenceRepository.deleteAllByApplicantId(command.applicantId());
        log.debug("Cleared existing preferences for applicant: {}", command.applicantId());

        // ------------------------------------------------------------------
        // Step 6: Build and save new preferences atomically
        // ------------------------------------------------------------------
        List<ApplicationPreference> preferences = buildPreferences(command, rankMark);
        List<ApplicationPreference> savedPreferences = preferenceRepository.saveAll(preferences);

        // ------------------------------------------------------------------
        // Step 7: Fire async email — OUTSIDE transaction control
        //         Runs after transaction commits, never blocks the response
        // ------------------------------------------------------------------
        fireConfirmationEmail(applicant, resolvedLotteries);

        return savedPreferences;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationPreference> getPreferences(UUID applicantId) {
        log.info("Getting preference for applicant: {}", applicantId);
        // Guard: applicant must exist
        if (!applicantRepository.existsById(applicantId)) {
            throw new ApplicantNotFoundException(applicantId);
        }
        return preferenceRepository.findByApplicantIdOrderByPreferenceOrderNum(applicantId);
    }


    // Private helpers

    private List<ApplicationPreference> buildPreferences(SubmitPreferenceCommand command, double rankMark) {

        return command.preferences().stream()
                .map(entry -> new ApplicationPreference(
                        UUID.randomUUID(),
                        command.applicantId(),
                        entry.lotteryId(),
                        rankMark,
                        entry.preferenceOrderNum(),
                        LocalDateTime.now()
                ))
                .collect(Collectors.toList());

    }

    private List<Lottery> resolveAndValidateLotteries(SubmitPreferenceCommand command, ApplicantProfile applicant, double rankMark) {

        List<Lottery> resolvedLotteries = new ArrayList<>();
        for (PreferenceEntry preference : command.preferences()) {
            Lottery lottery = lotteryRepository.findByIdWithCriteria(preference.lotteryId())
                    .orElseThrow(() -> new LotteryNotFoundException(preference.lotteryId()));
            boolean eligible = eligibilityEngine.isEligible(applicant, rankMark, lottery);
            if (!eligible) {
                throw new IneligibleLotteryException(command.applicantId(), preference.lotteryId());
            }
            resolvedLotteries.add(lottery);
        }
        return resolvedLotteries;
    }

    private void validatePreferenceListStructure(SubmitPreferenceCommand command) {
        List<PreferenceEntry> entries = command.preferences();
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Preference list must not be empty");
        }
        Set<UUID> seenLotteryIds = new HashSet<>();
        Set<Integer> seenOrderNums = new HashSet<>();

        for (PreferenceEntry entry : entries) {
            ApplicationPreference.validatePreferenceOrderNum(entry.preferenceOrderNum());

            if (!seenLotteryIds.add(entry.lotteryId())) {
                throw new DuplicatePreferenceException(command.applicantId(), entry.lotteryId());
            }

            if (!seenOrderNums.add(entry.preferenceOrderNum())) {
                throw new DuplicatePreferenceException(command.applicantId(), entry.preferenceOrderNum());
            }
        }

    }

    private void fireConfirmationEmail(ApplicantProfile applicant, List<Lottery> resolvedLotteries) {
        //TODO: will fire an event using Kafka
    }

}
