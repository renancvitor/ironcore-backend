package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WorkoutSessionActivityDetailResult(
        WorkoutSessionActivityId id,
        WorkoutActivityId workoutActivityId,
        ExerciseId exerciseId,
        String exerciseNameSnapshot,
        String activityTypeSnapshot,
        Integer orderIndexSnapshot,
        Integer plannedSetsSnapshot,
        Integer plannedRepRangeMinSnapshot,
        Integer plannedRepRangeMaxSnapshot,
        BigDecimal plannedTargetLoadKgSnapshot,
        String plannedTargetLoadTextSnapshot,
        Integer plannedDurationMinutesSnapshot,
        BigDecimal plannedDistanceKmSnapshot,
        String plannedIntensityTextSnapshot,
        Integer plannedRestSecondsSnapshot,
        String plannedNotesSnapshot,
        Integer actualDurationMinutes,
        BigDecimal actualDistanceKm,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        List<WorkoutSessionSetDetailResult> sets,
        ActualLoadMode actualLoadMode
) {

    public static WorkoutSessionActivityDetailResult from(WorkoutSessionActivity activity) {
        return new WorkoutSessionActivityDetailResult(
                activity.getId(),
                activity.getWorkoutActivityId(),
                activity.getExerciseId(),
                activity.getSnapshot().exerciseName(),
                activity.getSnapshot().activityType(),
                activity.getSnapshot().orderIndex(),
                activity.getSnapshot().plannedSets(),
                activity.getSnapshot().plannedRepRangeMin(),
                activity.getSnapshot().plannedRepRangeMax(),
                activity.getSnapshot().plannedTargetLoadKg(),
                activity.getSnapshot().plannedTargetLoadText(),
                activity.getSnapshot().plannedDurationMinutes(),
                activity.getSnapshot().plannedDistanceKm(),
                activity.getSnapshot().plannedIntensityText(),
                activity.getSnapshot().plannedRestSeconds(),
                activity.getSnapshot().plannedNotes(),
                activity.getActualDurationMinutes(),
                activity.getActualDistanceKm(),
                activity.getNotes(),
                activity.getCreatedAt(),
                activity.getUpdatedAt(),
                activity.getSets().stream().map(WorkoutSessionSetDetailResult::from).toList(),
                activity.getActualLoadMode()
        );
    }
}
