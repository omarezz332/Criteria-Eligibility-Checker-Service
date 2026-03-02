package com.eligibility.domain.service;

import com.eligibility.domain.model.ApplicantProfile;
import com.eligibility.domain.model.Lottery;
import com.eligibility.domain.model.LotteryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The core eligibility evaluation engine.
 * <p>
 * Evaluates one ApplicantProfile against one Lottery's criteria list.
 * Returns true only if ALL criteria pass — a single failure disqualifies.
 * <p>
 * How to add a new CriteriaType:
 * 1. Add the value to the CriteriaType enum (domain layer)
 * 2. Add a case here in evaluateSingleCriteria()
 * That's the only change needed — no schema migration, no other class changes.
 * <p>
 * This class is intentionally stateless — it holds no data, only logic.
 * Safe to use concurrently by any number of threads simultaneously.
 */
@Component
public class EligibilityEngine {
    private static final Logger log = LoggerFactory.getLogger(EligibilityEngine.class);

    public boolean isEligible(ApplicantProfile applicant, double rankMark, Lottery lottery) {
        List<LotteryCriteria> criteriaList = lottery.criteria();
        if (criteriaList.isEmpty()) {
            log.debug("Lottery [{}] has no criteria — applicant [{}] is eligible by default",
                    lottery.id(), applicant.id());
            return true;
        }
        for (LotteryCriteria criteria : criteriaList) {
            boolean passes = evaluateSingleCriteria(applicant, rankMark, criteria);
            if (!passes) {
                log.debug("Applicant [{}] FAILED criteria [{} = {}] for Lottery [{}]",
                        applicant.id(), criteria.criteriaType(),
                        criteria.criteriaValue(), lottery.id());
                return false;
            }
        }
        log.debug("Applicant [{}] PASSED all criteria for Lottery [{}]",
                applicant.id(), lottery.id());
        return true;
    }

    private boolean evaluateSingleCriteria(ApplicantProfile applicant,
                                           double rankMark,
                                           LotteryCriteria criteria) {
        return switch (criteria.criteriaType()) {
            case AGE_MIN -> applicant.age() >= (int) criteria.getValueAsDouble();
            case AGE_MAX -> applicant.age() <= (int) criteria.getValueAsDouble();
            case GENDER -> applicant.gender().name().equalsIgnoreCase(criteria.criteriaValue());
            case COUNTRY -> applicant.country().equalsIgnoreCase(criteria.criteriaValue());
            case NATIONALITY -> applicant.nationality().equalsIgnoreCase(criteria.criteriaValue());
            case MARITAL_STATUS -> applicant.maritalStatus().name().equalsIgnoreCase(criteria.criteriaValue());
            case HAS_DISABILITY -> applicant.hasDisability() == criteria.getValueAsBoolean();
            case MIN_RANK_MARK -> rankMark >= criteria.getValueAsDouble();
        };
    }
}
