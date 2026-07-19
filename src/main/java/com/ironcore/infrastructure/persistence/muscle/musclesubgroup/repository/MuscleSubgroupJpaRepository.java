package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.repository;

import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuscleSubgroupJpaRepository extends JpaRepository<MuscleSubgroupEntity, Long> {

    Optional<MuscleSubgroupEntity> findByCode(String code);

    List<MuscleSubgroupEntity> findByMuscleGroup_Id(Long muscleGroupId);
}
