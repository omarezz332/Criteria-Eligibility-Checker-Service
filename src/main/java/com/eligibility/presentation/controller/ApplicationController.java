package com.eligibility.presentation.controller;

import com.eligibility.application.dto.request.SubmitPreferenceRequest;
import com.eligibility.application.dto.response.EligibilityResponse;
import com.eligibility.application.dto.response.PreferenceResponse;
import com.eligibility.application.port.in.CheckEligibilityUseCase;
import com.eligibility.application.port.in.CheckEligibilityUseCase.EligibilityResult;
import com.eligibility.application.port.in.GetApplicantPreferencesUseCase;
import com.eligibility.application.port.in.SubmitPreferenceUseCase;
import com.eligibility.domain.model.ApplicationPreference;
import com.eligibility.presentation.mapper.PreferenceMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the applicant application flow.
 * <p>
 * Endpoints:
 * POST /api/applications/{applicantId}/eligibility  → check eligible lotteries
 * POST /api/applications/{applicantId}/preferences  → submit preference list
 * GET  /api/applications/{applicantId}/preferences  → view current preferences
 * <p>
 * applicantId is a path variable — not in the request body.
 * This is intentional: the resource being acted on is the applicant,
 * which belongs in the URL, not hidden in JSON.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final CheckEligibilityUseCase checkEligibilityUseCase;
    private final SubmitPreferenceUseCase submitPreferenceUseCase;
    private final GetApplicantPreferencesUseCase getPreferencesUseCase;
    private final PreferenceMapper preferenceMapper;

    public ApplicationController(
            CheckEligibilityUseCase checkEligibilityUseCase,
            SubmitPreferenceUseCase submitPreferenceUseCase,
            GetApplicantPreferencesUseCase getPreferencesUseCase,
            PreferenceMapper preferenceMapper
    ) {
        this.checkEligibilityUseCase = checkEligibilityUseCase;
        this.submitPreferenceUseCase = submitPreferenceUseCase;
        this.getPreferencesUseCase = getPreferencesUseCase;
        this.preferenceMapper = preferenceMapper;
    }

    /**
     * POST /api/applications/{applicantId}/eligibility
     * <p>
     * Check which active lotteries the applicant qualifies for.
     * Returns the applicant's rank mark and eligible lottery list.
     * Returns 200 even when the list is empty — empty is a valid result.
     */
    @PostMapping("/{applicantId}/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @PathVariable UUID applicantId
    ) {
        EligibilityResult result = checkEligibilityUseCase.checkEligibility(applicantId);
        return ResponseEntity.ok(preferenceMapper.toEligibilityResponse(result));
    }

    /**
     * POST /api/applications/{applicantId}/preferences
     * <p>
     * Submit the applicant's ordered preference list.
     * Returns 201 Created with the saved preferences.
     * Resubmission replaces the existing preference list entirely.
     * <p>
     * Possible errors:
     * 404 → applicant or lottery not found
     * 409 → concurrent update conflict (client should retry)
     * 422 → lottery submitted is not eligible for this applicant
     */
    @PostMapping("/{applicantId}/preferences")
    public ResponseEntity<PreferenceResponse> submitPreferences(
            @PathVariable UUID applicantId,
            @Valid @RequestBody SubmitPreferenceRequest request
    ) {
        List<ApplicationPreference> saved = submitPreferenceUseCase.submitPreferences(
                preferenceMapper.toCommand(applicantId, request)
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(preferenceMapper.toPreferenceResponse(applicantId, saved));
    }

    /**
     * GET /api/applications/{applicantId}/preferences
     * <p>
     * Retrieve the applicant's current preference list.
     * Returns 200 with an empty list if no preferences submitted yet.
     */
    @GetMapping("/{applicantId}/preferences")
    public ResponseEntity<PreferenceResponse> getPreferences(
            @PathVariable UUID applicantId
    ) {
        List<ApplicationPreference> preferences =
                getPreferencesUseCase.getPreferences(applicantId);
        return ResponseEntity.ok(preferenceMapper.toPreferenceResponse(applicantId, preferences));
    }
}