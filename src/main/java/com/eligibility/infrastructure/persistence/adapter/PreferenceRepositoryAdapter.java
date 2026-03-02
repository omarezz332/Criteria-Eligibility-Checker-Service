package com.eligibility.infrastructure.persistence.adapter;


import com.eligibility.application.port.out.PreferenceRepository;
import com.eligibility.domain.model.ApplicationPreference;
import com.eligibility.infrastructure.persistence.entity.ApplicationPreferenceEntity;
import com.eligibility.infrastructure.persistence.repository.PreferenceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PreferenceRepositoryAdapter implements PreferenceRepository {
    private final PreferenceJpaRepository preferenceJpaRepository;

    public PreferenceRepositoryAdapter(PreferenceJpaRepository preferenceJpaRepository) {
        this.preferenceJpaRepository = preferenceJpaRepository;
    }


    @Override
    public List<ApplicationPreference> saveAll(List<ApplicationPreference> preferences) {
        List<ApplicationPreferenceEntity> entities = new ArrayList<>();
        for (ApplicationPreference preference : preferences) {
            entities.add(toEntity(preference));
        }
        List<ApplicationPreferenceEntity> saved = preferenceJpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).toList();
    }

    @Override
    public List<ApplicationPreference> findByApplicantIdOrderByPreferenceOrderNum(UUID applicantId) {
        List<ApplicationPreferenceEntity> entityList =
                preferenceJpaRepository.findByApplicantIdOrderByPreferenceOrderNumAsc(applicantId);
        return entityList.stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteAllByApplicantId(UUID applicantId) {
        preferenceJpaRepository.deleteByApplicantId(applicantId);
    }

    @Override
    public boolean existsByApplicantId(UUID applicantId) {
        return preferenceJpaRepository.existsByApplicantId(applicantId);
    }

    // Translation: Domain → Entity
    private ApplicationPreferenceEntity toEntity(ApplicationPreference domain) {
        return new ApplicationPreferenceEntity(
                domain.id(),
                domain.applicantId(),
                domain.lotteryId(),
                domain.lotteryRankMark(),
                domain.preferenceOrderNum(),
                domain.createdDate()
        );
    }


    // Translation: Entity → Domain
    private ApplicationPreference toDomain(ApplicationPreferenceEntity entity) {
        return new ApplicationPreference(
                entity.getId(),
                entity.getApplicantId(),
                entity.getLotteryId(),
                entity.getLotteryRankMark(),
                entity.getPreferenceOrderNum(),
                entity.getCreatedDate()
        );
    }
}
