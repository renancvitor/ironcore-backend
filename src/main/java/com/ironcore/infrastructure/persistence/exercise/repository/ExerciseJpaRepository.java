package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, Long> {

    Optional<ExerciseEntity> findByIdAndActiveTrue(Long id);
}
