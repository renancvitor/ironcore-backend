package com.ironcore.application.workoutplanning.workoutactivity;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;

import java.math.BigDecimal;

public record WorkoutActivityAuditData(
        Long id,
        Long workoutDayId,
        Long exerciseId,
        Integer orderIndex,
        Integer sets,
        Integer repRangeMin,
        Integer repRangeMax,
        BigDecimal targetLoadKg,
        String targetLoadText,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String intensityText,
        Integer restSeconds,
        String notes
) implements LoggableData {

    public static WorkoutActivityAuditData from(WorkoutActivity workoutActivity) {
        return new WorkoutActivityAuditData(
                workoutActivity.getId().value(),
                workoutActivity.getWorkoutDayId().value(),
                workoutActivity.getExerciseId().value(),
                workoutActivity.getOrderIndex(),
                workoutActivity.getSets(),
                workoutActivity.getRepRangeMin(),
                workoutActivity.getRepRangeMax(),
                workoutActivity.getTargetLoadKg(),
                workoutActivity.getTargetLoadText(),
                workoutActivity.getDurationMinutes(),
                workoutActivity.getDistanceKm(),
                workoutActivity.getIntensityText(),
                workoutActivity.getRestSeconds(),
                workoutActivity.getNotes()
        );
    }
}
