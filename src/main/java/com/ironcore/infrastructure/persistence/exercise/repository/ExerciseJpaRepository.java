package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, Long>,
        JpaSpecificationExecutor<ExerciseEntity> {

    Optional<ExerciseEntity> findByIdAndActiveTrue(Long id);
}
