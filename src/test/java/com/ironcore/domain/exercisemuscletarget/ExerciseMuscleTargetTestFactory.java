package com.ironcore.domain.exercisemuscletarget;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;

import java.time.LocalDateTime;

public final class ExerciseMuscleTargetTestFactory {

    private ExerciseMuscleTargetTestFactory() {
    }

    public static ExerciseMuscleTarget restoreExerciseMuscleTarget() {
        return ExerciseMuscleTarget.restore(
                new ExerciseMuscleTargetId(1L),
                new ExerciseId(1L),
                new MuscleSubgroupId(1L),
                TargetRoleType.PRIMARY,
                true,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                LocalDateTime.of(2026, 7, 12, 11, 0)
        );
    }
}
