package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.mapper;

import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutday.entity.WorkoutDayEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.UPDATED_AT;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutday.WorkoutDayEntityTestFactory.workoutDayEntity;
import static org.assertj.core.api.Assertions.assertThat;

class WorkoutDayMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapWorkoutDayFields() {
            WorkoutDay workoutDay = restoredWorkoutDay();

            WorkoutDayEntity entity = WorkoutDayMapper.toEntity(workoutDay, workoutCycleEntity());

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getWorkoutCycle().getId()).isEqualTo(1L);
            assertThat(entity.getWeekDay()).isEqualTo(WeekDay.WEDNESDAY);
            assertThat(entity.getTitle()).isEqualTo("Treino de membros inferiores");
            assertThat(entity.getOrderIndex()).isEqualTo(2);
            assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreWorkoutDayFields() {
            WorkoutDayEntity entity = workoutDayEntity();

            WorkoutDay workoutDay = WorkoutDayMapper.toDomain(entity);

            assertThat(workoutDay.getId()).isEqualTo(new WorkoutDayId(1L));
            assertThat(workoutDay.getWorkoutCycleId()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(workoutDay.getWeekDay()).isEqualTo(WeekDay.WEDNESDAY);
            assertThat(workoutDay.getTitle()).isEqualTo("Treino de membros inferiores");
            assertThat(workoutDay.getOrderIndex()).isEqualTo(2);
            assertThat(workoutDay.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutDay.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }
}
