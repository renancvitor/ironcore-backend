package com.ironcore.infrastructure.persistence.exercisemuscletarget;

import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;

import java.time.LocalDateTime;

import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.muscleSubgroupEntity;

public final class ExerciseMuscleTargetEntityTestFactory {

    private ExerciseMuscleTargetEntityTestFactory() {
    }

    public static ExerciseMuscleTargetEntity exerciseMuscleTargetEntity() {
        return exerciseMuscleTargetEntity(1L);
    }

    public static ExerciseMuscleTargetEntity invalidExerciseMuscleTargetEntity() {
        return exerciseMuscleTargetEntity(null);
    }

    private static ExerciseMuscleTargetEntity exerciseMuscleTargetEntity(Long id) {
        return new ExerciseMuscleTargetEntity(
                id,
                exerciseEntity(),
                muscleSubgroupEntity(),
                TargetRoleType.PRIMARY,
                true,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 11, 0)
        );
    }
}
