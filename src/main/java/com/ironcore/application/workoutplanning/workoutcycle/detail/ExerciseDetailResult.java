package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.exercise.valueobject.ExerciseId;

import java.util.List;

public record ExerciseDetailResult(
        ExerciseId id,
        String name,
        List<MuscleGroupDetailResult> muscleGroups
) {
}
