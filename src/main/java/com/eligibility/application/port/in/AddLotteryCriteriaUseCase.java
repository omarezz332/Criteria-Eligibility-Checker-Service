package com.eligibility.application.port.in;

import com.eligibility.domain.model.LotteryCriteria;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Admin adds eligibility criteria to an existing Lottery.
 * <p>
 * Triggered by: POST /api/admin/lotteries/{lotteryId}/criteria
 * Implemented by: LotteryService
 * <p>
 * Accepts a batch of criteria at once — admin typically sets up
 * all criteria for a lottery in one operation.
 * <p>
 * Throws LotteryNotFoundException if the lotteryId does not exist.
 */
public interface AddLotteryCriteriaUseCase {

    List<LotteryCriteria> addCriteria(UUID lotteryId, List<AddCriteriaCommand> commands);

    record AddCriteriaCommand(
            String criteriaType,   // maps to CriteriaType enum
            String criteriaValue   // raw string value (e.g. "25", "MALE", "true")
    ) {
    }
}