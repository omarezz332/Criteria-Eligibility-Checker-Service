package com.eligibility.infrastructure.persistence.repository;

import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.infrastructure.persistence.entity.LotteryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the lottery table.
 * <p>
 * findAllActiveWithCriteria() is the most important query in the system.
 * It is called on every eligibility check — once per request under 25K users.
 * <p>
 * Using JOIN to avoid (N+1 problem).
 * JOIN FETCH loads everything in a single SQL JOIN — one round trip to DB.
 * <p>
 * This query result is then cached by the adapter so the DB is only
 * hit once per cache TTL, not once per request.
 */
public interface LotteryJpaRepository extends JpaRepository<LotteryEntity, UUID> {

    /**
     * Load all ACTIVE lotteries with their criteria in ONE query.
     * <p>
     * Generated SQL:
     * SELECT l.*, c.*
     * FROM lottery l
     * LEFT JOIN lottery_criteria c ON c.lottery_id = l.id
     * WHERE l.status = 'ACTIVE'
     * <p>
     * LEFT JOIN (not INNER JOIN) — so lotteries with no criteria
     * are still returned (open-to-all lotteries).
     */
    @Query("SELECT DISTINCT l FROM LotteryEntity l LEFT JOIN FETCH l.criteria WHERE l.status = :status")
    List<LotteryEntity> findAllByStatusWithCriteria(LotteryStatus status);
}