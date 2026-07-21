package com.ironcore.infrastructure.persistence.exercise.mapper;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.persistence.activitytype.entity.ActivityTypeEntity;
import com.ironcore.infrastructure.persistence.equipmenttype.entity.EquipmentTypeEntity;
import com.ironcore.infrastructure.persistence.exercise.entity.ExerciseEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static com.ironcore.infrastructure.persistence.activitytype.ActivityTypeEntityTestFactory.activityTypeEntity;
import static com.ironcore.infrastructure.persistence.equipmenttype.EquipmentTypeEntityTestFactory.equipmentTypeEntity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static org.assertj.core.api.Assertions.assertThat;

class ExerciseMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapExerciseFields() {
            Exercise exercise = restoreExercise();
            EquipmentTypeEntity equipmentType = equipmentTypeEntity();
            ActivityTypeEntity activityType = activityTypeEntity();

            ExerciseEntity entity = ExerciseMapper.toEntity(exercise, equipmentType, activityType);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getName()).isEqualTo("Supino reto");
            assertThat(entity.getEquipmentType()).isSameAs(equipmentType);
            assertThat(entity.getActivityType()).isSameAs(activityType);
            assertThat(entity.getUnilateral()).isFalse();
            assertThat(entity.getCompound()).isTrue();
            assertThat(entity.getSuggestedRestSeconds()).isEqualTo(90);
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getCreatedAt()).isEqualTo(exercise.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(exercise.getUpdatedAt());
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreExerciseFields() {
            ExerciseEntity entity = exerciseEntity();

            Exercise exercise = ExerciseMapper.toDomain(entity);

            assertThat(exercise.getId()).isEqualTo(new ExerciseId(1L));
            assertThat(exercise.getName()).isEqualTo("Supino reto");
            assertThat(exercise.getEquipmentTypeId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(exercise.getActivityTypeId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(exercise.getUnilateral()).isFalse();
            assertThat(exercise.getCompound()).isTrue();
            assertThat(exercise.getSuggestedRestSeconds()).isEqualTo(90);
            assertThat(exercise.getActive()).isTrue();
            assertThat(exercise.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(exercise.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }
}
