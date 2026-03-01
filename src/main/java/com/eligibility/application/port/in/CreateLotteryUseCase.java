package com.eligibility.application.port.in;

import com.eligibility.domain.model.Lottery;

/**
 * Use Case: Admin creates a new Lottery.
 * <p>
 * Triggered by: POST /api/admin/lotteries
 * Implemented by: LotteryService
 * <p>
 * New lotteries start with ACTIVE status by default.
 */
public interface CreateLotteryUseCase {

    Lottery create(CreateLotteryCommand command);

    record CreateLotteryCommand(
            String name
    ) {
    }
}