package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.repository;

import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutDayJpaRepository extends JpaRepository<WorkoutDayEntity, Long> {

    Optional<WorkoutDayEntity> findByIdAndWorkoutCycle_Person_Id(Long id, Long personId);

    List<WorkoutDayEntity> findByWorkoutCycle_IdOrderByOrderIndexAsc(Long workoutCycleId);
}
