package com.eligibility.domain.model;

import com.eligibility.domain.enums.CriteriaType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

/**
 * Represents a single eligibility rule attached to a Lottery.
 */
public record LotteryCriteria(UUID id, UUID lotteryId, CriteriaType criteriaType, String criteriaValue) {

    // -------------------------------------------------------------------------
    // Typed value helpers — so callers don't do raw string parsing themselves
    // -------------------------------------------------------------------------

    /**
     * Use when the criteria value represents a number (AGE_MIN, AGE_MAX, MIN_RANK_MARK)
     */
    @JsonIgnore
    public double getValueAsDouble() {
        try {
            return Double.parseDouble(criteriaValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Criteria [" + criteriaType + "] expects a numeric value but got: '" + criteriaValue + "'"
            );
        }
    }

    /**
     * Use when the criteria value represents a boolean (HAS_DISABILITY)
     */
    @JsonIgnore
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(criteriaValue);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "LotteryCriteria{" + criteriaType + "=" + criteriaValue + "}";
    }
}