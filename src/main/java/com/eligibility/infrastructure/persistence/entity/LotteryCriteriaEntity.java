package com.eligibility.infrastructure.persistence.entity;

import com.eligibility.domain.enums.CriteriaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA entity for the lottery_criteria table.
 * <p>
 * The @ManyToOne back-reference to LotteryEntity is needed by JPA
 * to manage the bidirectional relationship and to correctly
 * populate the lottery_id foreign key column.
 * <p>
 * FetchType.LAZY on the lottery side — when we load criteria,
 * we never need to navigate back up to the lottery entity.
 */
@Entity
@Table(name = "lottery_criteria")
public class LotteryCriteriaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lottery_id", nullable = false, updatable = false)
    private LotteryEntity lottery;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 50)
    private CriteriaType criteriaType;

    @Column(name = "criteria_value", nullable = false, length = 100)
    private String criteriaValue;

    protected LotteryCriteriaEntity() {
    }

    public LotteryCriteriaEntity(
            UUID id,
            LotteryEntity lottery,
            CriteriaType criteriaType,
            String criteriaValue
    ) {
        this.id = id;
        this.lottery = lottery;
        this.criteriaType = criteriaType;
        this.criteriaValue = criteriaValue;
    }

    public UUID getId() {
        return id;
    }

    public LotteryEntity getLottery() {
        return lottery;
    }

    public CriteriaType getCriteriaType() {
        return criteriaType;
    }

    public String getCriteriaValue() {
        return criteriaValue;
    }
}