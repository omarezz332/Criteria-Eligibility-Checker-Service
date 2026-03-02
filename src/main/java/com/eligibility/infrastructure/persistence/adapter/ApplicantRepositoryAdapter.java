package com.eligibility.infrastructure.persistence.adapter;

import com.eligibility.application.port.out.ApplicantRepository;
import com.eligibility.domain.model.ApplicantProfile;
import com.eligibility.infrastructure.persistence.entity.ApplicantProfileEntity;
import com.eligibility.infrastructure.persistence.repository.ApplicantJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements the ApplicantRepository outbound port.
 * <p>
 * This class is the only place that knows about both:
 * - The domain model (ApplicantProfile)
 * - The JPA entity (ApplicantProfileEntity)
 * <p>
 * The application layer only sees the ApplicantRepository interface.
 * JPA is invisible to services, use cases, and domain models.
 */
@Component
public class ApplicantRepositoryAdapter implements ApplicantRepository {

    private final ApplicantJpaRepository jpaRepository;

    public ApplicantRepositoryAdapter(ApplicantJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // -------------------------------------------------------------------------
    // ApplicantRepository port implementation
    // -------------------------------------------------------------------------

    @Override
    public ApplicantProfile save(ApplicantProfile applicant) {
        ApplicantProfileEntity entity = toEntity(applicant);
        ApplicantProfileEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<ApplicantProfile> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    // Translation: Domain → Entity
    private ApplicantProfileEntity toEntity(ApplicantProfile domain) {
        return new ApplicantProfileEntity(
                domain.id(),
                domain.name(),
                domain.age(),
                domain.gender(),
                domain.country(),
                domain.nationality(),
                domain.maritalStatus(),
                domain.hasDisability(),
                domain.disabilityName(),
                domain.educationLevel(),
                domain.careerYears(),
                domain.createdDate()
        );
    }

    // Translation: Entity → Domain
    // Called after reading from DB — maps the JPA entity to a pure domain object

    private ApplicantProfile toDomain(ApplicantProfileEntity entity) {
        return new ApplicantProfile(
                entity.getId(),
                entity.getName(),
                entity.getAge(),
                entity.getGender(),
                entity.getCountry(),
                entity.getNationality(),
                entity.getMaritalStatus(),
                entity.isHasDisability(),
                entity.getDisabilityName(),
                entity.getEducationLevel(),
                entity.getCareerYears(),
                entity.getCreatedDate()
        );
    }
}