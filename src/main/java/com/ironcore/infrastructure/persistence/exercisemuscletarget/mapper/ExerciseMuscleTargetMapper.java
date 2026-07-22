package com.ironcore.infrastructure.persistence.exercisemuscletarget.mapper;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;

public class ExerciseMuscleTargetMapper {

    public static ExerciseMuscleTargetEntity toEntity(
            ExerciseMuscleTarget exerciseMuscleTarget,
            ExerciseEntity exercise,
            MuscleSubgroupEntity muscleSubgroup
    ) {
        try {
            return new ExerciseMuscleTargetEntity(
                    exerciseMuscleTarget.getId() == null ? null : exerciseMuscleTarget.getId().value(),
                    exercise,
                    muscleSubgroup,
                    exerciseMuscleTarget.getTargetRole(),
                    exerciseMuscleTarget.getActive(),
                    exerciseMuscleTarget.getCreatedAt(),
                    exerciseMuscleTarget.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter músculo alvo do exercício de domínio para entidade.",
                    exception
            );
        }
    }

    public static ExerciseMuscleTarget toDomain(ExerciseMuscleTargetEntity exerciseMuscleTargetEntity) {
        try {
            return new ExerciseMuscleTarget(
                    new ExerciseMuscleTargetId(exerciseMuscleTargetEntity.getId()),
                    new ExerciseId(exerciseMuscleTargetEntity.getExercise().getId()),
                    new MuscleSubgroupId(exerciseMuscleTargetEntity.getMuscleSubgroup().getId()),
                    exerciseMuscleTargetEntity.getTargetRole(),
                    exerciseMuscleTargetEntity.getActive(),
                    exerciseMuscleTargetEntity.getCreatedAt(),
                    exerciseMuscleTargetEntity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException(
                    "Falha ao converter músculo alvo do exercício de entidade para domínio.",
                    exception
            );
        }
    }
}
