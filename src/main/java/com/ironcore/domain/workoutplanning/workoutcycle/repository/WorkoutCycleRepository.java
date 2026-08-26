package com.ironcore.domain.workoutplanning.workoutcycle.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;

import java.util.List;
import java.util.Optional;

public interface WorkoutCycleRepository {

    WorkoutCycle save(WorkoutCycle workoutCycle);

    Optional<WorkoutCycle> findByIdAndPersonId(WorkoutCycleId id, PersonId personId);

    List<WorkoutCycle> findByPersonId(PersonId personId);

    List<WorkoutCycle> findByPersonIdAndWorkoutStatus(PersonId personId, WorkoutStatus workoutStatus);

    List<WorkoutCycle> findByPersonIdAndTrainingGoalId(PersonId personId, TrainingGoalId trainingGoalId);

    void deleteById(WorkoutCycleId id);
}
