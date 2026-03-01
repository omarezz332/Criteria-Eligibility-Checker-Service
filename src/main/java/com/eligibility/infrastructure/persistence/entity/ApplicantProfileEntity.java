package com.eligibility.infrastructure.persistence.entity;

import com.eligibility.domain.enums.EducationLevel;
import com.eligibility.domain.enums.Gender;
import com.eligibility.domain.enums.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the applicant_profile table.
 * <p>
 * Intentionally separate from the domain model ApplicantProfile.
 * Reasons:
 * - Domain model is immutable (final fields, no setters)
 * JPA requires a no-args constructor and mutable fields
 * - Domain model has no @Entity, @Column, or any JPA annotation
 * - If the schema changes, only this class changes — domain is untouched
 * <p>
 * The adapter (ApplicantRepositoryAdapter) translates between
 * this entity and the domain model in both directions.
 */
@Entity
@Table(name = "applicant_profile")
public class ApplicantProfileEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "age", nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "nationality", nullable = false, length = 50)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "has_disability", nullable = false)
    private boolean hasDisability;

    @Column(name = "disability_name", length = 100)
    private String disabilityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false, length = 20)
    private EducationLevel educationLevel;

    @Column(name = "career_years", nullable = false)
    private int careerYears;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // -------------------------------------------------------------------------
    // JPA requires a public or protected no-args constructor
    // Keep it protected — no outside code should use it directly
    // -------------------------------------------------------------------------
    protected ApplicantProfileEntity() {
    }

    public ApplicantProfileEntity(
            UUID id,
            String name,
            int age,
            Gender gender,
            String country,
            String nationality,
            MaritalStatus maritalStatus,
            boolean hasDisability,
            String disabilityName,
            EducationLevel educationLevel,
            int careerYears,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.country = country;
        this.nationality = nationality;
        this.maritalStatus = maritalStatus;
        this.hasDisability = hasDisability;
        this.disabilityName = disabilityName;
        this.educationLevel = educationLevel;
        this.careerYears = careerYears;
        this.createdDate = createdDate;
    }

    // -------------------------------------------------------------------------
    // Getters — JPA needs these for reading back from the DB
    // No setters — we reconstruct via constructor, not mutation
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getCountry() {
        return country;
    }

    public String getNationality() {
        return nationality;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public boolean isHasDisability() {
        return hasDisability;
    }

    public String getDisabilityName() {
        return disabilityName;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public int getCareerYears() {
        return careerYears;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}