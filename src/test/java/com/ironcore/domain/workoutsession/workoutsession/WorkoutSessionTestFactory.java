package com.ironcore.domain.workoutsession.workoutsession;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySource;
import com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.restoreActivityType;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;

public final class WorkoutSessionTestFactory {

    public static final PersonId OWNER = new PersonId(1L);
    public static final Instant START = Instant.parse("2026-10-27T02:30:00Z");
    public static final WorkoutSessionActivityId ACTIVITY_ID = new WorkoutSessionActivityId(10L);
    public static final Instant EXPIRY = Instant.parse("2026-10-27T06:00:00Z");

    private WorkoutSessionTestFactory() {
    }

    public static WorkoutSession session() {
        return restoredSession(WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(restoredWorkoutActivity())), START));
    }

    public static WorkoutSession restoredSession(WorkoutSession source) {
        List<WorkoutSessionActivity> activities = new ArrayList<>();
        long activityId = 10L;
        long setId = 100L;
        for (WorkoutSessionActivity activity : source.getActivities()) {
            List<WorkoutSessionSet> sets = new ArrayList<>();
            for (WorkoutSessionSet set : activity.getSets()) {
                sets.add(WorkoutSessionSet.restore(set.getId() != null ? set.getId() : new WorkoutSessionSetId(setId++),
                        set.getSetNumber(), set.getActualLoadKg(), set.getActualLoadMode(), set.getActualReps(),
                        set.getNotes(), set.getCreatedAt(), set.getUpdatedAt()));
            }
            activities.add(WorkoutSessionActivity.restore(
                    activity.getId() != null ? activity.getId() : new WorkoutSessionActivityId(activityId++),
                    activity.getWorkoutActivityId(), activity.getExerciseId(), activity.getSnapshot(),
                    activity.getActualDurationMinutes(), activity.getActualDistanceKm(), activity.getNotes(),
                    activity.getCreatedAt(), activity.getUpdatedAt(), sets));
        }
        return WorkoutSession.restore(source.getId() != null ? source.getId() : new WorkoutSessionId(1L),
                source.getPersonId(), source.getWorkoutCycleId(), source.getWorkoutDayId(), source.getSnapshot(),
                source.getPerformedOn(), source.getStartedAt(), source.getFinishedAt(), source.getStatus(),
                source.getCloseReason(), source.getNotes(), source.getCreatedAt(), source.getUpdatedAt(),
                source.getVersion() != null ? source.getVersion() : 0L, activities);
    }

    public static WorkoutSessionActivitySource source(WorkoutActivity activity) {
        return new WorkoutSessionActivitySource(activity, restoreExercise(), restoreActivityType());
    }
}
