package com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject;

import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;

import java.math.BigDecimal;

public record WorkoutSessionActivitySnapshot(
        String exerciseName, String activityType, Integer orderIndex, Integer plannedSets,
        Integer plannedRepRangeMin, Integer plannedRepRangeMax, BigDecimal plannedTargetLoadKg,
        String plannedTargetLoadText, Integer plannedDurationMinutes, BigDecimal plannedDistanceKm,
        String plannedIntensityText, Integer plannedRestSeconds, String plannedNotes) {

    public WorkoutSessionActivitySnapshot {
        if (exerciseName == null || exerciseName.isBlank() || exerciseName.length() > 150
                || activityType == null || activityType.isBlank() || activityType.length() > 50
                || orderIndex == null || orderIndex <= 0) {
            throw new InvalidWorkoutSessionActivityException("Snapshot da atividade inválido.");
        }

        for (Integer value : new Integer[]{plannedSets, plannedRepRangeMin, plannedRepRangeMax,
                plannedDurationMinutes, plannedRestSeconds}) {
            if (value != null && value <= 0) {
                throw new InvalidWorkoutSessionActivityException("Valor planejado deve ser positivo.");
            }
        }

        if (plannedRepRangeMin != null && plannedRepRangeMax != null && plannedRepRangeMin > plannedRepRangeMax) {
            throw new InvalidWorkoutSessionActivityException("Faixa de repetições planejadas inválida.");
        }

        for (BigDecimal value : new BigDecimal[]{plannedTargetLoadKg, plannedDistanceKm}) {
            if (value != null && (value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                    || value.compareTo(new BigDecimal("999999.99")) > 0)) {
                throw new InvalidWorkoutSessionActivityException("Valor decimal planejado inválido.");
            }
        }

        if ((plannedTargetLoadText != null && plannedTargetLoadText.length() > 100)
                || (plannedIntensityText != null && plannedIntensityText.length() > 100)) {
            throw new InvalidWorkoutSessionActivityException("Texto planejado excede o tamanho máximo.");
        }
    }

    public static WorkoutSessionActivitySnapshot capture(WorkoutActivity activity, Exercise exercise, ActivityType type) {
        if (activity == null || exercise == null || type == null
                || !activity.getExerciseId().equals(exercise.getId())
                || !exercise.getActivityTypeId().equals(type.getId())) {
            throw new InvalidWorkoutSessionActivityException("Origem do snapshot da atividade inválida.");
        }

        return new WorkoutSessionActivitySnapshot(exercise.getName(), type.getCode().value(),
                activity.getOrderIndex(), activity.getSets(), activity.getRepRangeMin(), activity.getRepRangeMax(),
                activity.getTargetLoadKg(), activity.getTargetLoadText(), activity.getDurationMinutes(),
                activity.getDistanceKm(), activity.getIntensityText(), activity.getRestSeconds(), activity.getNotes());
    }
}
