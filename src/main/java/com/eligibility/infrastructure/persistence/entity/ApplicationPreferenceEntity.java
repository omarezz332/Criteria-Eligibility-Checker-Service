package com.eligibility.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the application_preference table.
 *
 * @Version (optimistic locking):
 * The version column is the concurrency guard for this table.
 * When two requests try to update the same applicant's preferences
 * simultaneously, JPA increments version on the first write.
 * The second write sees a version mismatch and throws
 * OptimisticLockingFailureException — caught in PreferenceService
 * and translated to ConcurrentPreferenceUpdateException → HTTP 409.
 * <p>
 * Unique constraints (enforced at DB level as a second safety net):
 * - (applicant_id, lottery_id)       — no duplicate lotteries per applicant
 * - (applicant_id, preference_order) — no duplicate order numbers per applicant
 * <p>
 * No @ManyToOne back-references here — preferences are a write-heavy table.
 * Avoiding JPA relationships keeps saves fast and avoids accidental lazy loads.
 * The adapter joins at the query level only when needed.
 */
@Entity
@Table(
        name = "application_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_applicant_lottery",
                        columnNames = {"applicant_id", "lottery_id"}
                ),
                @UniqueConstraint(
                        name = "uq_applicant_order",
                        columnNames = {"applicant_id", "preference_order_num"}
                )
        }
)
public class ApplicationPreferenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "applicant_id", nullable = false, updatable = false)
    private UUID applicantId;

    @Column(name = "lottery_id", nullable = false, updatable = false)
    private UUID lotteryId;

    @Column(name = "lottery_rank_mark", nullable = false, precision = 5, scale = 2)
    private double lotteryRankMark;

    @Column(name = "preference_order_num", nullable = false)
    private int preferenceOrderNum;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Optimistic lock version column.
     * JPA manages this automatically — never set it manually.
     * Starts at 0, increments on every update.
     */
    @Version
    @Column(name = "version", nullable = false)
    private int version;

    protected ApplicationPreferenceEntity() {
    }

    public ApplicationPreferenceEntity(
            UUID id,
            UUID applicantId,
            UUID lotteryId,
            double lotteryRankMark,
            int preferenceOrderNum,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.applicantId = applicantId;
        this.lotteryId = lotteryId;
        this.lotteryRankMark = lotteryRankMark;
        this.preferenceOrderNum = preferenceOrderNum;
        this.createdDate = createdDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicantId() {
        return applicantId;
    }

    public UUID getLotteryId() {
        return lotteryId;
    }

    public double getLotteryRankMark() {
        return lotteryRankMark;
    }

    public int getPreferenceOrderNum() {
        return preferenceOrderNum;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getVersion() {
        return version;
    }
}