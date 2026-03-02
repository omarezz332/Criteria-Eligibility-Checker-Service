package com.eligibility.presentation.controller;


import com.eligibility.application.dto.request.AddCriteriaRequest;
import com.eligibility.application.dto.request.CreateLotteryRequest;
import com.eligibility.application.dto.request.UpdateLotteryStatusRequest;
import com.eligibility.application.dto.response.CriteriaResponse;
import com.eligibility.application.dto.response.LotteryResponse;
import com.eligibility.application.port.in.AddLotteryCriteriaUseCase;
import com.eligibility.application.port.in.CreateLotteryUseCase;
import com.eligibility.application.port.in.UpdateLotteryStatusUseCase;
import com.eligibility.domain.enums.LotteryStatus;
import com.eligibility.domain.model.LotteryCriteria;
import com.eligibility.domain.model.Lottery;
import com.eligibility.presentation.mapper.LotteryMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/lotteries")
public class AdminLotteryController {
    private final CreateLotteryUseCase createLotteryUseCase;
    private final AddLotteryCriteriaUseCase addLotteryCriteriaUseCase;
    private final UpdateLotteryStatusUseCase updateLotteryStatusUseCase;
    private final LotteryMapper lotteryMapper;

    public AdminLotteryController(CreateLotteryUseCase createLotteryUseCase, AddLotteryCriteriaUseCase addLotteryCriteriaUseCase, UpdateLotteryStatusUseCase updateLotteryStatusUseCase, LotteryMapper lotteryMapper) {
        this.createLotteryUseCase = createLotteryUseCase;
        this.addLotteryCriteriaUseCase = addLotteryCriteriaUseCase;
        this.updateLotteryStatusUseCase = updateLotteryStatusUseCase;
        this.lotteryMapper = lotteryMapper;
    }

    /**
     * POST /api/admin/lotteries
     * Create a new lottery. Starts ACTIVE by default.
     */
    @PostMapping
    public ResponseEntity<LotteryResponse> createLottery(
            @Valid @RequestBody CreateLotteryRequest request
    ) {
        Lottery lottery = createLotteryUseCase.create(lotteryMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(lotteryMapper.toResponse(lottery));
    }

    /**
     * POST /api/admin/lotteries/{lotteryId}/criteria
     * Add eligibility criteria to an existing lottery.
     */
    @PostMapping("/{lotteryId}/criteria")
    public ResponseEntity<List<CriteriaResponse>> addCriteria(
            @PathVariable UUID lotteryId,
            @Valid @RequestBody AddCriteriaRequest request
    ) {
        List<LotteryCriteria> saved = addLotteryCriteriaUseCase.addCriteria(
                lotteryId,
                lotteryMapper.toCriteriaCommands(request)
        );
        List<CriteriaResponse> response = saved.stream()
                .map(c -> new CriteriaResponse(c.id(), c.criteriaType().name(), c.criteriaValue()))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{lotteryId}/status")
    public ResponseEntity<LotteryResponse> updateLottery(
            @PathVariable UUID lotteryId,
            @Valid @RequestBody UpdateLotteryStatusRequest request
    ) {
        LotteryStatus newStatus;
        try {
            newStatus = LotteryStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: '" + request.status() + "'. Accepted: ACTIVE, NOT_ACTIVE"
            );
        }
        Lottery update = updateLotteryStatusUseCase.updateStatus(lotteryId, newStatus);
        return ResponseEntity.ok(lotteryMapper.toResponse(update));

    }
}
