package com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.mapper;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.entity.WorkoutActivityEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.UPDATED_AT;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutactivity.WorkoutActivityEntityTestFactory.workoutActivityEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.workoutDayEntity;
import static org.assertj.core.api.Assertions.assertThat;

class WorkoutActivityMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapWorkoutActivityFields() {
            WorkoutActivity workoutActivity = restoredWorkoutActivity();

            WorkoutActivityEntity entity = WorkoutActivityMapper.toEntity(
                    workoutActivity,
                    workoutDayEntity(),
                    exerciseEntity()
            );

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getWorkoutDay().getId()).isEqualTo(1L);
            assertThat(entity.getExercise().getId()).isEqualTo(1L);
            assertThat(entity.getOrderIndex()).isEqualTo(2);
            assertThat(entity.getSets()).isEqualTo(5);
            assertThat(entity.getRepRangeMin()).isEqualTo(6);
            assertThat(entity.getRepRangeMax()).isEqualTo(10);
            assertThat(entity.getTargetLoadKg()).isEqualTo(new BigDecimal("90.00"));
            assertThat(entity.getTargetLoadText()).isEqualTo("RPE 9");
            assertThat(entity.getDurationMinutes()).isEqualTo(50);
            assertThat(entity.getDistanceKm()).isEqualTo(new BigDecimal("6.00"));
            assertThat(entity.getIntensityText()).isEqualTo("Alta");
            assertThat(entity.getRestSeconds()).isEqualTo(120);
            assertThat(entity.getNotes()).isEqualTo("Manter cadência controlada");
            assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreWorkoutActivityFields() {
            WorkoutActivityEntity entity = workoutActivityEntity();

            WorkoutActivity workoutActivity = WorkoutActivityMapper.toDomain(entity);

            assertThat(workoutActivity.getId()).isEqualTo(new WorkoutActivityId(1L));
            assertThat(workoutActivity.getWorkoutDayId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(workoutActivity.getExerciseId()).isEqualTo(new ExerciseId(1L));
            assertThat(workoutActivity.getOrderIndex()).isEqualTo(2);
            assertThat(workoutActivity.getSets()).isEqualTo(5);
            assertThat(workoutActivity.getRepRangeMin()).isEqualTo(6);
            assertThat(workoutActivity.getRepRangeMax()).isEqualTo(10);
            assertThat(workoutActivity.getTargetLoadKg()).isEqualTo(new BigDecimal("90.00"));
            assertThat(workoutActivity.getTargetLoadText()).isEqualTo("RPE 9");
            assertThat(workoutActivity.getDurationMinutes()).isEqualTo(50);
            assertThat(workoutActivity.getDistanceKm()).isEqualTo(new BigDecimal("6.00"));
            assertThat(workoutActivity.getIntensityText()).isEqualTo("Alta");
            assertThat(workoutActivity.getRestSeconds()).isEqualTo(120);
            assertThat(workoutActivity.getNotes()).isEqualTo("Manter cadência controlada");
            assertThat(workoutActivity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutActivity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }
}
