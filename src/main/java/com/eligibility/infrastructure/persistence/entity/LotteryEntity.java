package com.eligibility.infrastructure.persistence.entity;

import com.eligibility.domain.enums.LotteryStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the lottery table.
 * <p>
 * Owns a one-to-many relationship with LotteryCriteriaEntity.
 * <p>
 * FetchType.LAZY on criteria:
 * For admin operations (create, status update) we don't need criteria loaded.
 * The adapter explicitly calls findAllActiveWithCriteria() which uses
 * a JOIN FETCH query to load both in one round trip when needed.
 * Lazy is the safe default — it prevents accidental N+1 queries.
 */
@Entity
@Table(name = "lottery")
public class LotteryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LotteryStatus status;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * LAZY by default — only loaded when explicitly needed.
     * CascadeType.ALL — criteria are fully owned by the lottery.
     * orphanRemoval = true — removing from the list deletes the row.
     */
    @OneToMany(
            mappedBy = "lottery",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<LotteryCriteriaEntity> criteria = new ArrayList<>();

    protected LotteryEntity() {
    }

    public LotteryEntity(UUID id, String name, LotteryStatus status, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdDate = createdDate;
    }

    // -------------------------------------------------------------------------
    // Status is the only mutable field — updated by admin operations
    // -------------------------------------------------------------------------

    public void setStatus(LotteryStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LotteryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<LotteryCriteriaEntity> getCriteria() {
        return criteria;
    }
}