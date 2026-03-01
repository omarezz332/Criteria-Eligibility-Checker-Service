package com.eligibility.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * HTTP request body for POST /api/applicants
 *
 * Raw strings for enum fields (gender, maritalStatus, educationLevel)
 * because JSON has no concept of Java enums.
 * Parsing and validation into domain enums happens in ApplicantService.
 *
 * All validation annotations here are HTTP-level guards —
 * they catch obviously malformed requests before they reach the service.
 */
public record CreateApplicantRequest(

        @NotBlank(message = "name must not be blank")
        String name,

        @NotNull(message = "age is required")
        @Min(value = 18, message = "age must be at least 18")
        @Max(value = 100, message = "age must be at most 100")
        Integer age,

        @NotBlank(message = "gender is required")
        String gender,                  // "MALE" | "FEMALE"

        @NotBlank(message = "country is required")
        String country,

        @NotBlank(message = "nationality is required")
        String nationality,

        @NotBlank(message = "maritalStatus is required")
        String maritalStatus,           // "SINGLE" | "MARRIED" | "DIVORCED" | "WIDOWED"

        @NotNull(message = "hasDisability is required")
        Boolean hasDisability,

        String disabilityName,          // required only when hasDisability = true

        @NotBlank(message = "educationLevel is required")
        String educationLevel,          // "NONE" | "DIPLOMA" | "BSC" | "MSC" | "PHD"

        @NotNull(message = "careerYears is required")
        @Min(value = 0, message = "careerYears must be >= 0")
        Integer careerYears
) {}