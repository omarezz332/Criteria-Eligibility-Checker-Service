package com.eligibility.application.port.out;

import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.domain.model.Lottery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: Lottery persistence contract.
 * <p>
 * Implemented by: LotteryRepositoryAdapter (infrastructure layer)
 * <p>
 * findAllActiveWithCriteria() is the hot path — called on every eligibility check.
 * The infrastructure adapter caches its result with @Cacheable("active-lotteries").
 * The cache is evicted whenever a lottery is saved or its status changes.
 */
public interface LotteryRepository {

    /**
     * Persist a new or updated lottery.
     * Cache is evicted after this call.
     */
    Lottery save(Lottery lottery);

    /**
     * Find a lottery by ID — without criteria.
     * Used for admin operations (status updates, etc.).
     */
    Optional<Lottery> findById(UUID id);
    /**
     * Find a lottery by ID — with criteria.
     * Used for admin operations (status updates, etc.).
     */
    Optional<Lottery> findByIdWithCriteria(UUID id);
    /**
     * Load ALL active lotteries with their full criteria list.
     * <p>
     * This is the most performance-critical query in the system.
     * Called on every eligibility check — must be cached at the adapter level.
     * <p>
     * Returns lotteries with criteria eagerly loaded — no lazy loading,
     * no N+1, one query with a JOIN.
     */
    List<Lottery> findAllActiveWithCriteria();

    /**
     * Update a lottery's status.
     * Must evict the active lotteries cache after update.
     */
    Lottery updateStatus(UUID id, LotteryStatus newStatus);

    boolean existsByName(String name);
}