package com.eligibility.application.service;

import com.eligibility.application.port.in.CheckEligibilityUseCase;
import com.eligibility.application.port.out.ApplicantRepository;
import com.eligibility.application.port.out.LotteryRepository;
import com.eligibility.domain.exception.ApplicantNotFoundException;
import com.eligibility.domain.model.ApplicantProfile;
import com.eligibility.domain.model.Lottery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service: checks eligibility of an applicant across all active lotteries.
 * <p>
 * This is the most performance-critical service in the system.
 * Under 25K concurrent users, this will be the hottest code path.
 * <p>
 * Performance strategy:
 * - Read-only transaction (@Transactional(readOnly = true)) — no dirty checking overhead
 * - Lottery + criteria data loaded from cache (via LotteryRepository adapter)
 * - EligibilityEngine is stateless — safely shared across all threads
 * - Parallel stream for evaluating lotteries (500+ lotteries × criteria per each)
 * <p>
 * Flow:
 * 1. Load applicant profile (DB — by ID)
 * 2. Calculate rank mark (pure computation — no DB call)
 * 3. Load all active lotteries with criteria (cache hit in steady state)
 * 4. Evaluate each lottery in parallel using EligibilityEngine
 * 5. Return only passing lotteries wrapped in EligibilityResult
 */
@Service
@Transactional(readOnly = true)
public class EligibilityService implements CheckEligibilityUseCase {

    private static final Logger log = LoggerFactory.getLogger(EligibilityService.class);

    private final ApplicantRepository applicantRepository;
    private final LotteryRepository lotteryRepository;
    private final EligibilityEngine eligibilityEngine;

    public EligibilityService(
            ApplicantRepository applicantRepository,
            LotteryRepository lotteryRepository,
            EligibilityEngine eligibilityEngine
    ) {
        this.applicantRepository = applicantRepository;
        this.lotteryRepository = lotteryRepository;
        this.eligibilityEngine = eligibilityEngine;
    }

    // -------------------------------------------------------------------------
    // CheckEligibilityUseCase
    // -------------------------------------------------------------------------

    @Override
    public EligibilityResult checkEligibility(UUID applicantId) {
        log.info("Checking eligibility for applicant: {}", applicantId);

        // Step 1: Load applicant — throw if not found
        ApplicantProfile applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantNotFoundException(applicantId));

        // Step 2: Calculate rank mark once — reused for every lottery evaluation
        double rankMark = applicant.calculateRankMark();
        log.debug("Applicant [{}] rank mark: {}", applicantId, rankMark);

        // Step 3: Load all active lotteries with criteria (served from cache)
        List<Lottery> activeLotteries = lotteryRepository.findAllActiveWithCriteria();
        log.debug("Evaluating applicant [{}] against {} active lotteries", applicantId, activeLotteries.size());

        // Step 4: Evaluate in parallel — EligibilityEngine is stateless/thread-safe
        List<Lottery> eligibleLotteries = activeLotteries.parallelStream()
                .filter(lottery -> eligibilityEngine.isEligible(applicant, rankMark, lottery))
                .toList();

        log.info("Applicant [{}] is eligible for {}/{} lotteries",
                applicantId, eligibleLotteries.size(), activeLotteries.size());

        // Step 5: Return result
        return new EligibilityResult(applicantId, rankMark, eligibleLotteries);
    }
}