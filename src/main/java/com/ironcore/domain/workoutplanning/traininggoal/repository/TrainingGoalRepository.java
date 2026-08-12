package com.ironcore.domain.workoutplanning.traininggoal.repository;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;

import java.util.List;
import java.util.Optional;

public interface TrainingGoalRepository {

    Optional<TrainingGoal> findById(TrainingGoalId trainingGoalId);

    List<TrainingGoal> findAllActive();
}
