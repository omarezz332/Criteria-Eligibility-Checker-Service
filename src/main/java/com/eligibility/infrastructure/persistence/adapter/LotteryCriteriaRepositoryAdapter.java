package com.eligibility.infrastructure.persistence.adapter;

import com.eligibility.application.port.out.LotteryCriteriaRepository;
import com.eligibility.domain.model.LotteryCriteria;
import com.eligibility.infrastructure.persistence.entity.LotteryCriteriaEntity;
import com.eligibility.infrastructure.persistence.entity.LotteryEntity;
import com.eligibility.infrastructure.persistence.repository.LotteryCriteriaJpaRepository;
import com.eligibility.infrastructure.persistence.repository.LotteryJpaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Adapter: implements the LotteryCriteriaRepository outbound port.
 * Cache eviction on saveAll:
 * Adding criteria to a lottery changes its eligibility rules.
 * The active-lotteries cache must be cleared so the next eligibility
 * check picks up the new criteria.
 */

@Component
public class LotteryCriteriaRepositoryAdapter implements LotteryCriteriaRepository {

    private final LotteryCriteriaJpaRepository criteriaJpaRepository;
    private final LotteryJpaRepository lotteryJpaRepository;

    public LotteryCriteriaRepositoryAdapter(LotteryCriteriaJpaRepository criteriaJpaRepository, LotteryJpaRepository lotteryJpaRepository) {
        this.criteriaJpaRepository = criteriaJpaRepository;
        this.lotteryJpaRepository = lotteryJpaRepository;
    }

    @Override
    @CacheEvict(value = "active-lotteries", allEntries = true)
    public List<LotteryCriteria> saveAll(List<LotteryCriteria> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return List.of();
        }
        UUID lotteryId = criteriaList.get(0).lotteryId();
        LotteryEntity lotteryEntity = lotteryJpaRepository.findById(lotteryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lottery entity not found during criteria save: " + lotteryId
                ));
        List<LotteryCriteriaEntity> entities = new ArrayList<>();
        for (LotteryCriteria criteria : criteriaList) {
            entities.add(toEntity(criteria, lotteryEntity));
        }
        List<LotteryCriteriaEntity> saved = criteriaJpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).toList();
    }

    @Override
    public List<LotteryCriteria> findByLotteryId(UUID lotteryId) {
        List<LotteryCriteriaEntity> entities = criteriaJpaRepository.findByLotteryId(lotteryId);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    @CacheEvict(value = "active-lotteries", allEntries = true)
    public void deleteByLotteryId(UUID lotteryId) {
        criteriaJpaRepository.deleteByLotteryId(lotteryId);
    }


    // -------------------------------------------------------------------------
    // Translation: Domain → Entity
    // Requires the LotteryEntity reference for the @ManyToOne FK
    // -------------------------------------------------------------------------

    private LotteryCriteriaEntity toEntity(LotteryCriteria domain, LotteryEntity lotteryEntity) {
        return new LotteryCriteriaEntity(
                domain.id(),
                lotteryEntity,
                domain.criteriaType(),
                domain.criteriaValue()
        );
    }

    // -------------------------------------------------------------------------
    // Translation: Entity → Domain
    // getLottery().getId() — only the ID is needed, not the full lottery object
    // -------------------------------------------------------------------------

    private LotteryCriteria toDomain(LotteryCriteriaEntity entity) {
        return new LotteryCriteria(
                entity.getId(),
                entity.getLottery().getId(),
                entity.getCriteriaType(),
                entity.getCriteriaValue()
        );
    }

}