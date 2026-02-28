package com.eligibility.domain.model;

import com.eligibility.domain.enums.EducationLevel;
import com.eligibility.domain.enums.Gender;
import com.eligibility.domain.enums.MaritalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core domain model representing an applicant's profile.
 * <p>
 * This is a plain Java class — no Spring, no JPA annotations.
 * The infrastructure layer maps this to/from database entities.
 *
 * @param disabilityName null when hasDisability = false
 */
public record ApplicantProfile(UUID id, String name, int age, Gender gender, String country, String nationality,
                               MaritalStatus maritalStatus, boolean hasDisability, String disabilityName,
                               EducationLevel educationLevel, int careerYears, LocalDateTime createdDate) {

    // -------------------------------------------------------------------------
    // Constructor (all fields, used by the infrastructure adapter when loading)
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Core Business Rule: Rank Mark Calculation
    //
    // Formula: RankMark = (educationWeight * 40%) + (careerWeight * 60%)
    //
    // Education Weights:  NONE=0, DIPLOMA=30, BSC=50, MSC=80, PHD=100
    // Career Weights:     0 yrs=0, 1-3=30, 4-6=50, 7-9=70, >9=100
    //
    // Example: BSC(50) + 3yrs(30) → (50*0.4) + (30*0.6) = 20+18 = 38.0
    // -------------------------------------------------------------------------

    public double calculateRankMark() {
        double educationWeight = educationLevel.getWeight();
        double careerWeight = resolveCareerWeight();

        return (educationWeight * 0.40) + (careerWeight * 0.60);
    }

    private double resolveCareerWeight() {
        if (careerYears == 0) return 0;
        if (careerYears <= 3) return 30;
        if (careerYears <= 6) return 50;
        if (careerYears <= 9) return 70;
        return 100;
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — domain model is immutable after creation)
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ApplicantProfile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rankMark=" + calculateRankMark() +
                '}';
    }
}
