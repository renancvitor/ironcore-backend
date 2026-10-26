package com.ironcore.domain.workoutsession.workoutsession;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;

import java.time.Instant;
import java.util.List;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.restoreActivityType;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;

public final class WorkoutSessionTestFactory {
    public static final PersonId OWNER = new PersonId(1L);
    public static final Instant START = Instant.parse("2026-10-27T02:30:00Z");
    public static final Instant EXPIRY = Instant.parse("2026-10-27T06:00:00Z");

    private WorkoutSessionTestFactory() {}

    public static WorkoutSession session() {
        return WorkoutSession.start(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(restoredWorkoutActivity())), START);
    }

    public static WorkoutSessionActivitySource source(WorkoutActivity activity) {
        return new WorkoutSessionActivitySource(activity, restoreExercise(), restoreActivityType());
    }
}
