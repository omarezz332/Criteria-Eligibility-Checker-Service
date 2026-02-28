package com.eligibility.domain.model;

import com.eligibility.domain.enums.LotteryStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Lottery that applicants can apply to.
 *
 * @param criteria Criteria attached to this lottery. Loaded eagerly for eligibility checks.
 */
public record Lottery(UUID id, String name, LotteryStatus status, LocalDateTime createdDate,
                      List<LotteryCriteria> criteria) {

    public Lottery(
            UUID id,
            String name,
            LotteryStatus status,
            LocalDateTime createdDate,
            List<LotteryCriteria> criteria
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdDate = createdDate;
        this.criteria = criteria != null ? new ArrayList<>(criteria) : new ArrayList<>();
    }

    /**
     * Convenience constructor — when criteria are not yet loaded
     */
    public Lottery(UUID id, String name, LotteryStatus status, LocalDateTime createdDate) {
        this(id, name, status, createdDate, null);
    }

    // -------------------------------------------------------------------------
    // Business helpers
    // -------------------------------------------------------------------------

    public boolean isActive() {
        return LotteryStatus.ACTIVE.equals(this.status);
    }

    public boolean hasCriteria() {
        return !criteria.isEmpty();
    }

    /**
     * Returns an unmodifiable view — criteria are set at construction time
     */
    @Override
    public List<LotteryCriteria> criteria() {
        return Collections.unmodifiableList(criteria);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Lottery{id=" + id + ", name='" + name + "', status=" + status + "}";
    }
}