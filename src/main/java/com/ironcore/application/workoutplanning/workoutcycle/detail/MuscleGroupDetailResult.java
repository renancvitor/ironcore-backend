package com.ironcore.application.workoutplanning.workoutcycle.detail;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;

public record MuscleGroupDetailResult(
        MuscleGroupId id,
        String name
) {
}
