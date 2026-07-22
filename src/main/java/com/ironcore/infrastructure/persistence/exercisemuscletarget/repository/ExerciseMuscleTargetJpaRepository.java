package com.ironcore.infrastructure.persistence.exercisemuscletarget.repository;

import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseMuscleTargetJpaRepository extends JpaRepository<ExerciseMuscleTargetEntity, Long> {
}
