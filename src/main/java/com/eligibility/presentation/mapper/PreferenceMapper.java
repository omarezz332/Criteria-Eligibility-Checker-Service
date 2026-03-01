package com.eligibility.presentation.mapper;

import com.eligibility.application.dto.request.SubmitPreferenceRequest;
import com.eligibility.application.dto.response.EligibilityResponse;
import com.eligibility.application.dto.response.LotteryResponse;
import com.eligibility.application.dto.response.PreferenceResponse;
import com.eligibility.application.port.in.CheckEligibilityUseCase.EligibilityResult;
import com.eligibility.application.port.in.SubmitPreferenceUseCase.PreferenceEntry;
import com.eligibility.application.port.in.SubmitPreferenceUseCase.SubmitPreferenceCommand;
import com.eligibility.domain.model.ApplicationPreference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps between preference-related domain models and HTTP DTOs.
 */
@Component
public class PreferenceMapper {

    private final LotteryMapper lotteryMapper;

    public PreferenceMapper(LotteryMapper lotteryMapper) {
        this.lotteryMapper = lotteryMapper;
    }

    /**
     * HTTP request → Use case command.
     * applicantId comes from the path variable, not the request body —
     * the controller passes it in separately.
     */
    public SubmitPreferenceCommand toCommand(UUID applicantId, SubmitPreferenceRequest request) {
        List<PreferenceEntry> entries = new ArrayList<>();
        for (SubmitPreferenceRequest.PreferenceEntryRequest entry : request.preferences()) {
            entries.add(new PreferenceEntry(entry.lotteryId(), entry.preferenceOrderNum()));
        }
        return new SubmitPreferenceCommand(applicantId, entries);
    }

    /**
     * EligibilityResult domain object → HTTP response.
     * Builds a human-readable message depending on whether any lotteries matched.
     */
    public EligibilityResponse toEligibilityResponse(EligibilityResult result) {
        List<LotteryResponse> lotteryResponses = lotteryMapper.toResponseList(result.eligibleLotteries());

        String message = result.hasEligibleLotteries()
                ? "You are eligible for " + lotteryResponses.size() + " lottery(s)."
                : "No eligible lotteries found for your profile.";

        return new EligibilityResponse(
                result.applicantId(),
                result.rankMark(),
                lotteryResponses,
                message
        );
    }

    /**
     * List of saved ApplicationPreference domain objects → HTTP response.
     */
    public PreferenceResponse toPreferenceResponse(UUID applicantId, List<ApplicationPreference> preferences) {
        List<PreferenceResponse.PreferenceEntry> entries = new ArrayList<>();
        for (ApplicationPreference pref : preferences) {
            entries.add(new PreferenceResponse.PreferenceEntry(
                    pref.id(),
                    pref.lotteryId(),
                    pref.lotteryRankMark(),
                    pref.preferenceOrderNum(),
                    pref.createdDate()
            ));
        }

        return new PreferenceResponse(
                applicantId,
                entries,
                "Preferences submitted successfully."
        );
    }
}