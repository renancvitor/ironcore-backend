package com.ironcore.infrastructure.persistence.activitytype.repository;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.repository.ActivityTypeRepository;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.activitytype.mapper.ActivityTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ActivityTypeAdapter implements ActivityTypeRepository {

    private final ActivityTypeJpaRepository activityTypeJpaRepository;

    @Override
    public Optional<ActivityType> findById(ActivityTypeId id) {
        Optional<ActivityTypeEntity> entity;
        try {
            Long activityTypeId = Objects.requireNonNull(
                    id.value(),
                    "Id do tipo de atividade não pode ser nulo."
            );
            entity = activityTypeJpaRepository.findById(activityTypeId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar activity type por id.", exception);
        }

        try {
            return entity.map(ActivityTypeMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter activity type por id para domínio.", exception);
        }
    }

    @Override
    public Optional<ActivityType> findByCode(ActivityTypeCode code) {
        Optional<ActivityTypeEntity> entity;
        try {
            String activityTypeCode = Objects.requireNonNull(
                    code.value(),
                    "Code do tipo de atividade não pode ser nulo."
            );
            entity = activityTypeJpaRepository.findByCode(activityTypeCode);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar activity type por code.", exception);
        }

        try {
            return entity.map(ActivityTypeMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter activity type por code para domínio.", exception);
        }
    }
}
