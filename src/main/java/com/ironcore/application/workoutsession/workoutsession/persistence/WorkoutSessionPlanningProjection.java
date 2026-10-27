package com.ironcore.application.workoutsession.workoutsession.persistence;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionSnapshot;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;

import java.util.List;

public record WorkoutSessionPlanningProjection(
        PersonId personId,
        WorkoutCycleId cycleId,
        WorkoutDayId dayId,
        WorkoutStatus status,
        WorkoutSessionSnapshot snapshot,
        List<Activity> activities
) {
    public WorkoutSessionPlanningProjection {
        activities = List.copyOf(activities);
    }

    public record Activity(
            WorkoutActivityId activityId,
            ExerciseId exerciseId,
            WorkoutSessionActivitySnapshot snapshot
    ) {
    }
}
