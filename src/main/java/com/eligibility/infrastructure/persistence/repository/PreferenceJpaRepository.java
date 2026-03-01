package com.eligibility.infrastructure.persistence.repository;

import com.eligibility.infrastructure.persistence.entity.ApplicationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the application_preference table.
 *
 * The two most critical operations here are:
 *   1. deleteAllByApplicantId — fires inside the submission transaction
 *      before the new preferences are saved (replace semantics)
 *   2. findByApplicantIdOrderByPreferenceOrderNumAsc — reads the preference
 *      list back in the correct display order
 */
public interface PreferenceJpaRepository extends JpaRepository<ApplicationPreferenceEntity, UUID> {

    /**
     * Load all preferences for an applicant ordered by their chosen priority.
     * Spring Data derives the query from the method name — no @Query needed.
     */
    List<ApplicationPreferenceEntity> findByApplicantIdOrderByPreferenceOrderNumAsc(UUID applicantId);

    /**
     * Bulk delete — same reasoning as LotteryCriteriaJpaRepository.
     * One DELETE statement, not N individual deletes.
     * clearAutomatically = true prevents stale first-level cache reads.
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ApplicationPreferenceEntity p WHERE p.applicantId = :applicantId")
    void deleteByApplicantId(@Param("applicantId") UUID applicantId);

    /**
     * Existence check without loading the full entity.
     * Used in GetApplicantPreferencesUseCase to confirm
     * the applicant exists before querying preferences.
     */
    boolean existsByApplicantId(UUID applicantId);
}