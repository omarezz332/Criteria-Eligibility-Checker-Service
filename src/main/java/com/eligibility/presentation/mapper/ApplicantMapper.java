package com.eligibility.presentation.mapper;

import com.eligibility.application.dto.request.CreateApplicantRequest;
import com.eligibility.application.dto.response.ApplicantResponse;
import com.eligibility.application.port.in.RegisterApplicantUseCase.RegisterApplicantCommand;
import com.eligibility.domain.model.ApplicantProfile;
import org.springframework.stereotype.Component;

@Component
public class ApplicantMapper {

    public RegisterApplicantCommand toCommand(CreateApplicantRequest request) {
        return new RegisterApplicantCommand(
                request.name(),
                request.age(),
                request.gender(),
                request.country(),
                request.nationality(),
                request.maritalStatus(),
                request.hasDisability(),
                request.disabilityName(),
                request.educationLevel(),
                request.careerYears()
        );
    }

    public ApplicantResponse toResponse(ApplicantProfile profile) {
        return new ApplicantResponse(
                profile.id(),
                profile.name(),
                profile.age(),
                profile.gender().name(),
                profile.country(),
                profile.nationality(),
                profile.maritalStatus().name(),
                profile.hasDisability(),
                profile.disabilityName(),
                profile.educationLevel().name(),
                profile.careerYears(),
                profile.calculateRankMark(),
                profile.createdDate()
        );
    }
}
