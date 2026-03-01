package com.eligibility.application.port.in;

import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.domain.model.Lottery;

import java.util.UUID;

/**
 * Use Case: Admin activates or deactivates a Lottery.
 *
 * Triggered by: PATCH /api/admin/lotteries/{lotteryId}/status
 * Implemented by: LotteryService
 *
 * Deactivating a lottery removes it from eligibility checks immediately
 * (cache is evicted on status change).
 *
 * Throws LotteryNotFoundException if lotteryId does not exist.
 */
public interface UpdateLotteryStatusUseCase {

    Lottery updateStatus(UUID lotteryId, LotteryStatus newStatus);
}