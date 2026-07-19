package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.repository;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.repository.MuscleSubgroupRepository;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.mapper.MuscleSubgroupMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class MuscleSubgroupAdapter implements MuscleSubgroupRepository {

    private MuscleSubgroupJpaRepository muscleSubgroupJpaRepository;

    @Override
    public Optional<MuscleSubgroup> findById(MuscleSubgroupId id) {
        Optional<MuscleSubgroupEntity> entity;
        try {
            Long muscleSubgroupId = Objects.requireNonNull(
                    id.value(),
                    "Id do subgrupo muscular não pode ser nulo."
            );
            entity = muscleSubgroupJpaRepository.findById(muscleSubgroupId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle subgroup por id.", exception);
        }

        try {
            return entity.map(MuscleSubgroupMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter subgrupo muscular de entidade para domínio.", exception);
        }
    }

    @Override
    public Optional<MuscleSubgroup> findByCode(MuscleSubgroupCode code) {
        Optional<MuscleSubgroupEntity> entity;
        try {
            String muscleSubgroupCode = Objects.requireNonNull(
                    code.value(),
                    "Code do subgrupo muscular não pode ser nulo."
            );
            entity = muscleSubgroupJpaRepository.findByCode(muscleSubgroupCode);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle subgroup por code.", exception);
        }

        try {
            return entity.map(MuscleSubgroupMapper::toDomain);
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter subgrupo muscular de entidade para domínio.", exception);
        }
    }

    @Override
    public List<MuscleSubgroup> findByMuscleGroupId(MuscleGroupId id) {
        List<MuscleSubgroupEntity> entity;
        try {
            Long muscleGroupId = Objects.requireNonNull(
                    id.value(),
                    "Id do grupo muscular não pode ser nulo."
            );
            entity = muscleSubgroupJpaRepository.findByMuscleGroup_Id(muscleGroupId);
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle subgroup por muscle group id.", exception);
        }

        try {
            return entity.stream().map(MuscleSubgroupMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter subgrupo muscular de entidade para domínio.", exception);
        }
    }

    @Override
    public List<MuscleSubgroup> findAll() {
        List<MuscleSubgroupEntity> entity;
        try {
            entity = muscleSubgroupJpaRepository.findAll();
        } catch (RuntimeException exception) {
            throw new PersistenceException("Falha ao buscar muscle subgroups.", exception);
        }

        try {
            return entity.stream().map(MuscleSubgroupMapper::toDomain).toList();
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter muscle subgroups.", exception);
        }
    }
}
