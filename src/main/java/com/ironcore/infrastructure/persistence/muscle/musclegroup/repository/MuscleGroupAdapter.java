package com.ironcore.infrastructure.persistence.muscle.musclegroup.repository;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.repository.MuscleGroupRepository;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import com.ironcore.infrastructure.persistence.muscle.musclegroup.mapper.MuscleGroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MuscleGroupAdapter implements MuscleGroupRepository {

    private final MuscleGroupJpaRepository muscleGroupJpaRepository;

    @Override
    public Optional<MuscleGroup> findById(MuscleGroupId id) {
        Optional<MuscleGroupEntity> entity;
        try {
            Long muscleGroupId = Objects.requireNonNull(
                    id.value(),
                    "Id do grupo muscular não pode ser nulo."
            );
            entity = muscleGroupJpaRepository.findById(muscleGroupId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle group por id.", exception);
        }

        try {
            return entity.map(MuscleGroupMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter grupo muscular de entidade para domínio.", exception);
        }
    }

    @Override
    public Optional<MuscleGroup> findByCode(MuscleGroupCode code) {
        Optional<MuscleGroupEntity> entity;
        try {
            String muscleGroupCode = Objects.requireNonNull(
                    code.value(),
                    "Code do grupo muscular não pode ser nulo."
            );
            entity = muscleGroupJpaRepository.findByCode(muscleGroupCode);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle group por code.", exception);
        }

        try {
            return entity.map(MuscleGroupMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter grupo muscular de entidade para domínio.", exception);
        }
    }
}
