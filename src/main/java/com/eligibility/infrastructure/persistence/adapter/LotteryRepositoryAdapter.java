package com.eligibility.infrastructure.persistence.adapter;

import com.eligibility.application.port.out.LotteryRepository;
import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.domain.model.Lottery;
import com.eligibility.domain.model.LotteryCriteria;
import com.eligibility.infrastructure.persistence.entity.LotteryCriteriaEntity;
import com.eligibility.infrastructure.persistence.entity.LotteryEntity;
import com.eligibility.infrastructure.persistence.repository.LotteryJpaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements the LotteryRepository outbound port.
 *
 * Caching strategy:
 *   findAllActiveWithCriteria() → @Cacheable("active-lotteries")
 *     The single most called method in the system. Under 25K concurrent
 *     eligibility checks, this would hammer the DB on every request.
 *     Cached — the DB is hit once per cache TTL regardless of user count.
 *
 *   save() and updateStatus() → @CacheEvict("active-lotteries")
 * Cache TTL and provider are configured in application.yml (Caffeine).
 * The adapter knows nothing about how the cache works internally —
 * it just declares the intent with annotations.
 */

@Component
public class LotteryRepositoryAdapter implements LotteryRepository {
    private final LotteryJpaRepository jpaRepository;

    public LotteryRepositoryAdapter(LotteryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // Save a new lottery and evict the cache.
    @Override
    @CacheEvict(value = "active-lotteries", allEntries = true)
    public Lottery save(Lottery lottery) {
        LotteryEntity lotteryEntity = toEntity(lottery);
        LotteryEntity saved = jpaRepository.save(lotteryEntity);
        return toDomainWithoutCriteria(saved);
    }

    // Find lottery by ID — used for admin operations.
    @Override
    public Optional<Lottery> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainWithoutCriteria);
    }

    @Override
    public Optional<Lottery> findByIdWithCriteria(UUID id) {
        return jpaRepository.findById(id).map(this::toDomainWithCriteria);
    }

    // called on every single eligibility check.

    @Override
    @Cacheable("active-lotteries")
    public List<Lottery> findAllActiveWithCriteria() {
        List<Lottery> result = new ArrayList<>();
        List<LotteryEntity> lotteryEntities = jpaRepository.findAllByStatusWithCriteria(LotteryStatus.ACTIVE);
        for (LotteryEntity lotteryEntity : lotteryEntities) {
            result.add(toDomainWithCriteria(lotteryEntity));
        }
        return result;
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    @CacheEvict(value = "active-lotteries", allEntries = true)
    public Lottery updateStatus(UUID id, LotteryStatus newStatus) {
        LotteryEntity lotteryEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lottery not found during status update: " + id
                ));
        lotteryEntity.setStatus(newStatus);
        LotteryEntity updated = jpaRepository.save(lotteryEntity);
        return toDomainWithoutCriteria(updated);
    }

    // Translation: Domain → Entity
    // Used when saving a new lottery — criteria are managed separately
    private LotteryEntity toEntity(Lottery domain) {
        return new LotteryEntity(
                domain.id(),
                domain.name(),
                domain.status(),
                domain.createdDate()
        );
    }

    // Translation: Entity → Domain (without criteria)
    // Used for admin operations — criteria not needed
    private Lottery toDomainWithoutCriteria(LotteryEntity entity) {
        return new Lottery(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedDate()
        );
    }

    // Translation: Entity → Domain (with criteria)
    private Lottery toDomainWithCriteria(LotteryEntity entity) {
        List<LotteryCriteria> criteria = new ArrayList<>();
        for (LotteryCriteriaEntity criteriaEntity : entity.getCriteria()) {
            criteria.add(toCriteriaDomain(criteriaEntity));
        }

        return new Lottery(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedDate(),
                criteria
        );
    }

    private LotteryCriteria toCriteriaDomain(LotteryCriteriaEntity entity) {
        return new LotteryCriteria(
                entity.getId(),
                entity.getLottery().getId(),
                entity.getCriteriaType(),
                entity.getCriteriaValue()
        );
    }
}
