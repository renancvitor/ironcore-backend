package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.repository;

import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingGoalJpaRepository extends JpaRepository<TrainingGoalEntity, Long> {

    List<TrainingGoalEntity> findAllByActiveTrueOrderBySortOrderAscDisplayNameAsc();
}
