package com.ironcore.domain.workoutplanning.workoutday.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.util.List;
import java.util.Optional;

public interface WorkoutDayRepository {

    WorkoutDay save(WorkoutDay workoutDay);

    Optional<WorkoutDay> findByIdAndPersonId(WorkoutDayId id, PersonId personId);

    List<WorkoutDay> findByWorkoutCycleId(WorkoutCycleId workoutCycleId);

    void deleteById(WorkoutDayId id);
}
