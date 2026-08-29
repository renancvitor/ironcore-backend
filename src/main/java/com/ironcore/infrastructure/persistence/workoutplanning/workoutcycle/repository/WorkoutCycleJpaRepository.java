package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.repository;

import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WorkoutCycleJpaRepository extends JpaRepository<WorkoutCycleEntity, Long>,
        JpaSpecificationExecutor<WorkoutCycleEntity> {

    Optional<WorkoutCycleEntity> findByIdAndPerson_Id(Long id, Long personId);

    List<WorkoutCycleEntity> findByPerson_Id(Long personId);

    List<WorkoutCycleEntity> findByPerson_IdAndWorkoutStatus(Long personId, WorkoutStatus workoutStatus);

    List<WorkoutCycleEntity> findByPerson_IdAndTrainingGoal_Id(Long personId, Long trainingGoalId);
}
