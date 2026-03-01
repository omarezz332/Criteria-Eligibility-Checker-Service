package com.eligibility.application.service;

import com.eligibility.application.port.in.AddLotteryCriteriaUseCase;
import com.eligibility.application.port.in.CreateLotteryUseCase;
import com.eligibility.application.port.in.UpdateLotteryStatusUseCase;
import com.eligibility.application.port.out.LotteryCriteriaRepository;
import com.eligibility.application.port.out.LotteryRepository;
import com.eligibility.domain.enums.CriteriaType;
import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.domain.exception.LotteryNotFoundException;
import com.eligibility.domain.model.Lottery;
import com.eligibility.domain.model.LotteryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service: handles all admin lottery management operations.
 * <p>
 * Implements:
 * - CreateLotteryUseCase
 * - AddLotteryCriteriaUseCase
 * - UpdateLotteryStatusUseCase
 * <p>
 * Cache eviction on save/update is handled transparently by the
 * LotteryRepositoryAdapter — this service doesn't need to know about it.
 */
@Service
public class LotteryService implements
        CreateLotteryUseCase,
        AddLotteryCriteriaUseCase,
        UpdateLotteryStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(LotteryService.class);

    private final LotteryRepository lotteryRepository;
    private final LotteryCriteriaRepository criteriaRepository;

    public LotteryService(
            LotteryRepository lotteryRepository,
            LotteryCriteriaRepository criteriaRepository
    ) {
        this.lotteryRepository = lotteryRepository;
        this.criteriaRepository = criteriaRepository;
    }

    // -------------------------------------------------------------------------
    // CreateLotteryUseCase
    // -------------------------------------------------------------------------

    @Override
    public Lottery create(CreateLotteryCommand command) {
        log.info("Creating lottery: name={}", command.name());

        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Lottery name must not be blank");
        }

        Lottery lottery = new Lottery(
                UUID.randomUUID(),
                command.name().trim(),
                LotteryStatus.ACTIVE,    // all new lotteries start ACTIVE
                LocalDateTime.now()
        );

        Lottery saved = lotteryRepository.save(lottery);

        log.info("Lottery created: id={}, name={}", saved.id(), saved.name());
        return saved;
    }

    // -------------------------------------------------------------------------
    // AddLotteryCriteriaUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<LotteryCriteria> addCriteria(UUID lotteryId, List<AddCriteriaCommand> commands) {
        log.info("Adding {} criteria to lottery: {}", commands.size(), lotteryId);

        // Guard: lottery must exist before adding criteria
        lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new LotteryNotFoundException(lotteryId));

        List<LotteryCriteria> criteriaList = new ArrayList<>();
        for (AddCriteriaCommand cmd : commands) {
            criteriaList.add(buildCriteria(lotteryId, cmd));
        }

        List<LotteryCriteria> saved = criteriaRepository.saveAll(criteriaList);

        log.info("Saved {} criteria for lottery: {}", saved.size(), lotteryId);
        return saved;
    }

    // -------------------------------------------------------------------------
    // UpdateLotteryStatusUseCase
    // -------------------------------------------------------------------------

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public Lottery updateStatus(UUID lotteryId, LotteryStatus newStatus) {
        log.info("Updating lottery [{}] status to: {}", lotteryId, newStatus);

        // Guard: lottery must exist
        lotteryRepository.findById(lotteryId)
                .orElseThrow(() -> new LotteryNotFoundException(lotteryId));

        Lottery updated = lotteryRepository.updateStatus(lotteryId, newStatus);

        log.info("Lottery [{}] status updated to: {}", lotteryId, newStatus);
        return updated;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private LotteryCriteria buildCriteria(UUID lotteryId, AddCriteriaCommand cmd) {
        CriteriaType criteriaType = parseCriteriaType(cmd.criteriaType());
        validateCriteriaValue(criteriaType, cmd.criteriaValue());

        return new LotteryCriteria(
                UUID.randomUUID(),
                lotteryId,
                criteriaType,
                cmd.criteriaValue().trim()
        );
    }

    private CriteriaType parseCriteriaType(String rawType) {
        try {
            return CriteriaType.valueOf(rawType.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown criteria type: '" + rawType + "'. " +
                            "Accepted values: " + java.util.Arrays.toString(CriteriaType.values())
            );
        }
    }

    /**
     * Validates that the criteria value makes sense for its type.
     * Catches admin configuration mistakes early — before they cause
     * silent evaluation errors inside the eligibility engine.
     */
    private void validateCriteriaValue(CriteriaType type, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Criteria value must not be blank for type: " + type
            );
        }

        switch (type) {
            case AGE_MIN, AGE_MAX, MIN_RANK_MARK -> {
                try {
                    double parsed = Double.parseDouble(value);
                    if (parsed < 0) throw new IllegalArgumentException(
                            "Criteria [" + type + "] value must be non-negative, got: " + value
                    );
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Criteria [" + type + "] expects a numeric value, got: '" + value + "'"
                    );
                }
            }
            case HAS_DISABILITY -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            "Criteria [HAS_DISABILITY] expects 'true' or 'false', got: '" + value + "'"
                    );
                }
            }
            // GENDER, COUNTRY, NATIONALITY, MARITAL_STATUS: free-text string match
            // Values validated at runtime by the eligibility engine against the applicant's enum
            default -> {
            }
        }
    }
}