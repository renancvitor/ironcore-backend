package com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.mapper;

import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.entity.TrainingGoalEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.restoreTrainingGoal;
import static com.ironcore.infrastructure.persistence.workoutplanning.traininggoal.TrainingGoalEntityTestFactory.trainingGoalEntity;
import static org.assertj.core.api.Assertions.assertThat;

class TrainingGoalMapperTest {

    @Nested
    class ToEntity {

        @Test
        void shouldMapCatalogFields() {
            TrainingGoal trainingGoal = restoreTrainingGoal();

            TrainingGoalEntity entity = TrainingGoalMapper.toEntity(trainingGoal);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCode()).isEqualTo("HYPERTROPHY");
            assertThat(entity.getDisplayName()).isEqualTo("Hipertrofia");
            assertThat(entity.getActive()).isTrue();
            assertThat(entity.getSortOrder()).isEqualTo(10);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldRestoreCatalogFields() {
            TrainingGoalEntity entity = trainingGoalEntity();

            TrainingGoal trainingGoal = TrainingGoalMapper.toDomain(entity);

            assertThat(trainingGoal.getId()).isEqualTo(new TrainingGoalId(1L));
            assertThat(trainingGoal.getCode()).isEqualTo(new TrainingGoalCode("HYPERTROPHY"));
            assertThat(trainingGoal.getDisplayName()).isEqualTo("Hipertrofia");
            assertThat(trainingGoal.getActive()).isTrue();
            assertThat(trainingGoal.getSortOrder()).isEqualTo(10);
        }
    }
}
