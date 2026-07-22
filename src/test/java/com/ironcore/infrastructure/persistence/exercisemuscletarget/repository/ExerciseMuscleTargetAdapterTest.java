package com.ironcore.infrastructure.persistence.exercisemuscletarget.repository;

import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.exercisemuscletarget.model.ExerciseMuscleTarget;
import com.ironcore.domain.exercisemuscletarget.valueobject.ExerciseMuscleTargetId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ironcore.infrastructure.persistence.exercisemuscletarget.ExerciseMuscleTargetEntityTestFactory.exerciseMuscleTargetEntity;
import static com.ironcore.infrastructure.persistence.exercisemuscletarget.ExerciseMuscleTargetEntityTestFactory.invalidExerciseMuscleTargetEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseMuscleTargetAdapterTest {

    @Mock
    private ExerciseMuscleTargetJpaRepository exerciseMuscleTargetJpaRepository;

    @InjectMocks
    private ExerciseMuscleTargetAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindExerciseMuscleTargetById() {
            when(exerciseMuscleTargetJpaRepository.findById(1L)).thenReturn(Optional.of(exerciseMuscleTargetEntity()));

            Optional<ExerciseMuscleTarget> result = adapter.findById(new ExerciseMuscleTargetId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new ExerciseMuscleTargetId(1L));
            assertThat(result.get().getExerciseId()).isEqualTo(new ExerciseId(1L));
            assertThat(result.get().getMuscleSubgroupId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(result.get().getTargetRole()).isEqualTo(TargetRoleType.PRIMARY);
            assertThat(result.get().getActive()).isTrue();
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(exerciseMuscleTargetJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<ExerciseMuscleTarget> result = adapter.findById(new ExerciseMuscleTargetId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(exerciseMuscleTargetJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            ExerciseMuscleTargetId exerciseMuscleTargetId = new ExerciseMuscleTargetId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(exerciseMuscleTargetId))
                    .withMessage("Falha ao buscar músculo alvo do exercício por id");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(exerciseMuscleTargetJpaRepository.findById(1L))
                    .thenReturn(Optional.of(invalidExerciseMuscleTargetEntity()));
            ExerciseMuscleTargetId exerciseMuscleTargetId = new ExerciseMuscleTargetId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(exerciseMuscleTargetId))
                    .withMessage("Falha ao converter músculo alvo do exercício de entidade para domínio.");
        }
    }
}
