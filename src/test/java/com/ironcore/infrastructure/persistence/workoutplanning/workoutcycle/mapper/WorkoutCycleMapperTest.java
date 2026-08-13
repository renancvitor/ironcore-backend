package com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.mapper;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.entity.WorkoutCycleEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.CREATED_AT;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.START_DATE;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.UPDATED_AT;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.trainingGoalEntity;
import static com.ironcore.infrastructure.persistence.workoutplanning.workoutcycle.WorkoutCycleEntityTestFactory.workoutCycleEntity;
import static org.assertj.core.api.Assertions.assertThat;

class WorkoutCycleMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapWorkoutCycleFields() {
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();

            WorkoutCycleEntity entity = WorkoutCycleMapper.toEntity(
                    workoutCycle,
                    personEntity(),
                    trainingGoalEntity()
            );

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getPerson().getId()).isEqualTo(1L);
            assertThat(entity.getName()).isEqualTo("Ciclo de hipertrofia");
            assertThat(entity.getTrainingGoal().getId()).isEqualTo(1L);
            assertThat(entity.getStartDate()).isEqualTo(START_DATE);
            assertThat(entity.getEndDate()).isNull();
            assertThat(entity.getDesiredDurationMonths()).isEqualTo(3);
            assertThat(entity.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(entity.getWorkoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(entity.getNotes()).isEqualTo("Planejamento restaurado.");
            assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreWorkoutCycleFields() {
            WorkoutCycleEntity entity = workoutCycleEntity();

            WorkoutCycle workoutCycle = WorkoutCycleMapper.toDomain(entity);

            assertThat(workoutCycle.getId()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(workoutCycle.getPersonId()).isEqualTo(new PersonId(1L));
            assertThat(workoutCycle.getName()).isEqualTo("Ciclo de hipertrofia");
            assertThat(workoutCycle.getTrainingGoalId()).isEqualTo(new TrainingGoalId(1L));
            assertThat(workoutCycle.getStartDate()).isEqualTo(START_DATE);
            assertThat(workoutCycle.getEndDate()).isNull();
            assertThat(workoutCycle.getDesiredDurationMonths()).isEqualTo(3);
            assertThat(workoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
            assertThat(workoutCycle.getWorkoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(workoutCycle.getNotes()).isEqualTo("Planejamento restaurado.");
            assertThat(workoutCycle.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(workoutCycle.getUpdatedAt()).isEqualTo(UPDATED_AT);
        }
    }
}
