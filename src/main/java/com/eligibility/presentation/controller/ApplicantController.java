package com.eligibility.presentation.controller;

import com.eligibility.application.dto.request.CreateApplicantRequest;
import com.eligibility.application.dto.response.ApplicantResponse;
import com.eligibility.application.port.in.RegisterApplicantUseCase;
import com.eligibility.domain.model.ApplicantProfile;
import com.eligibility.presentation.mapper.ApplicantMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for applicant registration.
 * <p>
 * POST /api/applicants
 */
@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    private final RegisterApplicantUseCase registerApplicantUseCase;
    private final ApplicantMapper applicantMapper;

    public ApplicantController(
            RegisterApplicantUseCase registerApplicantUseCase,
            ApplicantMapper applicantMapper
    ) {
        this.registerApplicantUseCase = registerApplicantUseCase;
        this.applicantMapper = applicantMapper;
    }

    /**
     * POST /api/applicants
     * Register a new applicant profile.
     * Returns 201 Created with the saved profile + calculated rank mark.
     */
    @PostMapping
    public ResponseEntity<ApplicantResponse> register(
            @Valid @RequestBody CreateApplicantRequest request
    ) {
        ApplicantProfile saved = registerApplicantUseCase.register(
                applicantMapper.toCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(applicantMapper.toResponse(saved));
    }
}