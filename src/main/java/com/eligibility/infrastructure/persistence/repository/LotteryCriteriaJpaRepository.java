package com.eligibility.infrastructure.persistence.repository;

import com.eligibility.infrastructure.persistence.entity.LotteryCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the lottery_criteria table.
 */
public interface LotteryCriteriaJpaRepository extends JpaRepository<LotteryCriteriaEntity, UUID> {

    List<LotteryCriteriaEntity> findByLotteryId(UUID lotteryId);

    /**
     * Bulk delete by lotteryId.
     *
     * Why @Query + @Modifying instead of Spring Data derived delete?
     *   Spring Data's deleteBy... methods first SELECT all matching rows,
     *   then delete them one by one — that's N+1 deletes.
     *   A single DELETE WHERE is one SQL statement regardless of row count.
     *
     * @Modifying tells Spring Data this query changes state (not a SELECT).
     * clearAutomatically = true evicts the deleted entities from the
     * first-level cache so subsequent reads don't return stale data.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM LotteryCriteriaEntity c WHERE c.lottery.id = :lotteryId")
    void deleteByLotteryId(@Param("lotteryId") UUID lotteryId);
}