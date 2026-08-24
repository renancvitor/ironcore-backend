package com.ironcore.domain.workoutplanning.workoutactivity.repository;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;

import java.util.List;
import java.util.Optional;

public interface WorkoutActivityRepository {

    WorkoutActivity save(WorkoutActivity workoutActivity);

    Optional<WorkoutActivity> findByIdAndPersonId(WorkoutActivityId id, PersonId personId);

    List<WorkoutActivity> findByPersonIdAndWorkoutDayId(PersonId personId, WorkoutDayId workoutDayId);

    boolean existsByPersonIdAndWorkoutDayIdAndExerciseId(
            PersonId personId,
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId
    );

    boolean existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
            PersonId personId,
            WorkoutDayId workoutDayId,
            ExerciseId exerciseId,
            WorkoutActivityId id
    );

    void deleteById(WorkoutActivityId id);
}
