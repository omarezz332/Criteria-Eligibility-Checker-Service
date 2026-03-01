package com.eligibility.infrastructure.persistence.repository;

import com.eligibility.infrastructure.persistence.entity.ApplicantProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for applicant_profile table.
 * <p>
 * Spring generates the implementation at runtime.
 * The adapter wraps this and translates entities ↔ domain models.
 * <p>
 * findById and existsById are inherited from JpaRepository —
 * no need to declare them.
 */
public interface ApplicantJpaRepository extends JpaRepository<ApplicantProfileEntity, UUID> {
}