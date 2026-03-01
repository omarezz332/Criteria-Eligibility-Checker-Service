package com.eligibility.application.port.out;

import com.eligibility.domain.model.LotteryCriteria;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port: Lottery criteria persistence contract.
 * <p>
 * Implemented by: LotteryCriteriaRepositoryAdapter (infrastructure layer)
 * <p>
 * Criteria are always owned by a Lottery — you never load criteria
 * without knowing which lottery they belong to.
 */
public interface LotteryCriteriaRepository {

    /**
     * Save a batch of criteria for a given lottery.
     * Returns the saved criteria with generated IDs.
     */
    List<LotteryCriteria> saveAll(List<LotteryCriteria> criteriaList);

    /**
     * Load all criteria for a specific lottery.
     * Used when loading a single lottery's detail for admin views.
     */
    List<LotteryCriteria> findByLotteryId(UUID lotteryId);

    /**
     * Delete all criteria for a lottery.
     * Used before replacing criteria (admin re-configures a lottery).
     */
    void deleteByLotteryId(UUID lotteryId);
}