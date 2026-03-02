package com.eligibility.application.service;

import com.eligibility.application.port.in.RegisterApplicantUseCase;
import com.eligibility.application.port.out.ApplicantRepository;
import com.eligibility.domain.enums.EducationLevel;
import com.eligibility.domain.enums.Gender;
import com.eligibility.domain.enums.MaritalStatus;
import com.eligibility.domain.model.ApplicantProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ApplicantService implements RegisterApplicantUseCase {
    private static final Logger log = LoggerFactory.getLogger(ApplicantService.class);
    ApplicantRepository applicantRepository;

    public ApplicantService(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    @Override
    @Transactional
    public ApplicantProfile register(RegisterApplicantCommand command) {
        log.info("Registering new applicant: name={}", command.name());

        Gender gender = parseEnum(Gender.class, command.gender(), "gender");
        MaritalStatus maritalStatus = parseEnum(MaritalStatus.class, command.maritalStatus(), "maritalStatus");
        EducationLevel educationLevel = parseEnum(EducationLevel.class, command.educationLevel(), "educationLevel");

        validateAge(command.age());
        validateCareerYears(command.careerYears());
        validateDisabilityConsistency(command.hasDisability(), command.disabilityName());

        ApplicantProfile applicant = new ApplicantProfile(
                UUID.randomUUID(),
                command.name().trim(),
                command.age(),
                gender,
                command.country().trim(),
                command.nationality().trim(),
                maritalStatus,
                command.hasDisability(),
                command.hasDisability() ? command.disabilityName() : null,
                educationLevel,
                command.careerYears(),
                LocalDateTime.now()
        );
        ApplicantProfile saved = applicantRepository.save(applicant);
        log.info("Applicant registered successfully: id={}, rankMark={}",
                saved.id(), saved.calculateRankMark());
        return saved;
    }


    private void validateAge(int age) {
        if (age < 18 || age > 100) {
            throw new IllegalArgumentException(
                    "Age must be between 18 and 100, got: " + age
            );
        }
    }

    private void validateCareerYears(int years) {
        if (years < 0) {
            throw new IllegalArgumentException(
                    "Career years cannot be negative, got: " + years
            );
        }
    }

    private void validateDisabilityConsistency(boolean hasDisability, String disabilityName) {
        if (hasDisability && (disabilityName == null || disabilityName.isBlank())) {
            throw new IllegalArgumentException(
                    "disabilityName is required when hasDisability is true"
            );
        }
    }

    /**
     * Parse a raw string value into the target enum type.
     * Case-insensitive — "male", "MALE", "Male" all work.
     * Throws IllegalArgumentException with a clear message on invalid values.
     */
    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for " + fieldName + ": '" + value + "'. " +
                            "Accepted values: " + java.util.Arrays.toString(enumClass.getEnumConstants())
            );
        }
    }
}
