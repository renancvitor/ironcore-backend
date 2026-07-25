package com.ironcore.infrastructure.persistence.muscle.musclegroup.repository;

import com.ironcore.infrastructure.persistence.muscle.musclegroup.entity.MuscleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuscleGroupJpaRepository extends JpaRepository<MuscleGroupEntity, Long> {

    Optional<MuscleGroupEntity> findByCode(String code);

    List<MuscleGroupEntity> findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
}
