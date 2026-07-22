package com.ironcore.domain.exercisemuscletarget.model;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.exercisemuscletarget.exception.InvalidExerciseMuscleTargetException;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExerciseMuscleTarget {

    private final ExerciseMuscleTargetId id;
    private final ExerciseId exerciseId;
    private final MuscleSubgroupId muscleSubgroupId;
    private final TargetRoleType targetRole;
    private final Boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ExerciseMuscleTarget(ExerciseMuscleTargetId id, ExerciseId exerciseId, MuscleSubgroupId muscleSubgroupId,
                                 TargetRoleType targetRole, Boolean active, LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        this.id = requireNonNull(id, "Id não pode ser nulo.");
        this.exerciseId = requireNonNull(exerciseId, "Id do exercício não pode ser nulo.");
        this.muscleSubgroupId = requireNonNull(muscleSubgroupId, "Id do subgrupo muscular não pode ser nulo.");
        this.targetRole = requireNonNull(targetRole, "Papel do músculo alvo do exercício não pode ser nulo.");
        this.active = requireNonNull(active, "Tag de músculo alvo do exercício ativo não pode ser nulo.");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo.");
        this.updatedAt = updatedAt;
    }

    public static ExerciseMuscleTarget restore(ExerciseMuscleTargetId id, ExerciseId exerciseId,
                                               MuscleSubgroupId muscleSubgroupId, TargetRoleType targetRole,
                                               Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ExerciseMuscleTarget(id, exerciseId,  muscleSubgroupId, targetRole, active, createdAt, updatedAt);
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidExerciseMuscleTargetException(message);
        }

        return value;
    }
}
