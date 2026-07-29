package com.ironcore.infrastructure.persistence.exercisemuscletarget.repository;

import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseMuscleTargetJpaRepository extends JpaRepository<ExerciseMuscleTargetEntity, Long> {

    List<ExerciseMuscleTargetEntity> findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(
            Long exerciseId
    );
}
