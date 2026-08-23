package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.repository;

import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutActivityJpaRepository extends JpaRepository<WorkoutActivityEntity, Long> {

    Optional<WorkoutActivityEntity> findByIdAndWorkoutDay_WorkoutCycle_Person_Id(Long id, Long personId);

    List<WorkoutActivityEntity> findByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdOrderByOrderIndexAsc(
            Long personId,
            Long workoutDayId
    );

    boolean existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_Id(
            Long personId,
            Long workoutDayId,
            Long exerciseId
    );

    boolean existsByWorkoutDay_WorkoutCycle_Person_IdAndWorkoutDay_IdAndExercise_IdAndIdNot(
            Long personId,
            Long workoutDayId,
            Long exerciseId,
            Long id
    );
}
