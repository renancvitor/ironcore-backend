package com.ironcore.infrastructure.persistence.exercisemuscletarget.mapper;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.entity.ExerciseMuscleTargetEntity;
import com.ironcore.infrastructure.persistence.muscle.musclesubgroup.entity.MuscleSubgroupEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.exercisemuscletarget.ExerciseMuscleTargetTestFactory.restoreExerciseMuscleTarget;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.exercisemuscletarget.ExerciseMuscleTargetEntityTestFactory.exerciseMuscleTargetEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.muscleSubgroupEntity;
import static org.assertj.core.api.Assertions.assertThat;

class ExerciseMuscleTargetMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapExerciseMuscleTargetFields() {
            ExerciseMuscleTarget exerciseMuscleTarget = restoreExerciseMuscleTarget();
            ExerciseEntity exercise = exerciseEntity();
            MuscleSubgroupEntity muscleSubgroup = muscleSubgroupEntity();

            ExerciseMuscleTargetEntity entity = ExerciseMuscleTargetMapper.toEntity(
                    exerciseMuscleTarget,
                    exercise,
                    muscleSubgroup
            );

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getExercise()).isSameAs(exercise);
            assertThat(entity.getMuscleSubgroup()).isSameAs(muscleSubgroup);
            assertThat(entity.getTargetRole()).isEqualTo(TargetRoleType.PRIMARY);
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getCreatedAt()).isEqualTo(exerciseMuscleTarget.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(exerciseMuscleTarget.getUpdatedAt());
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreExerciseMuscleTargetFields() {
            ExerciseMuscleTargetEntity entity = exerciseMuscleTargetEntity();

            ExerciseMuscleTarget exerciseMuscleTarget = ExerciseMuscleTargetMapper.toDomain(entity);

            assertThat(exerciseMuscleTarget.getId()).isEqualTo(new ExerciseMuscleTargetId(1L));
            assertThat(exerciseMuscleTarget.getExerciseId()).isEqualTo(new ExerciseId(1L));
            assertThat(exerciseMuscleTarget.getMuscleSubgroupId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(exerciseMuscleTarget.getTargetRole()).isEqualTo(TargetRoleType.PRIMARY);
            assertThat(exerciseMuscleTarget.getActive()).isTrue();
            assertThat(exerciseMuscleTarget.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(exerciseMuscleTarget.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }
}
