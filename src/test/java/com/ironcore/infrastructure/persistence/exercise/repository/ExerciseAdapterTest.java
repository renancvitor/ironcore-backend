package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.invalidExerciseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseAdapterTest {

    @Mock
    private ExerciseJpaRepository exerciseJpaRepository;

    @InjectMocks
    private ExerciseAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindExerciseById() {
            when(exerciseJpaRepository.findById(1L)).thenReturn(Optional.of(exerciseEntity()));

            Optional<Exercise> result = adapter.findById(new ExerciseId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new ExerciseId(1L));
            assertThat(result.get().getName()).isEqualTo("Supino reto");
            assertThat(result.get().getEquipmentTypeId()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(result.get().getActivityTypeId()).isEqualTo(new ActivityTypeId(1L));
            assertThat(result.get().getUnilateral()).isFalse();
            assertThat(result.get().getCompound()).isTrue();
            assertThat(result.get().getSuggestedRestSeconds()).isEqualTo(90);
            assertThat(result.get().getActive()).isTrue();
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(exerciseJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Exercise> result = adapter.findById(new ExerciseId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(exerciseJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            ExerciseId exerciseId = new ExerciseId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(exerciseId))
                    .withMessage("Falha ao buscar exercise por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(exerciseJpaRepository.findById(1L)).thenReturn(Optional.of(invalidExerciseEntity()));
            ExerciseId exerciseId = new ExerciseId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(exerciseId))
                    .withMessage("Falha ao converter exercício de entidade para domínio.");
        }
    }
}
