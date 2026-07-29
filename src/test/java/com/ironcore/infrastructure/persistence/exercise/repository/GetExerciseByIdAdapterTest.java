package com.ironcore.infrastructure.persistence.exercise.repository;

import com.ironcore.application.exercise.usecase.GetExerciseByIdResult;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeCode;
import com.ironcore.domain.equipmenttype.valueobject.EquipmentTypeId;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.exercisemuscletarget.enums.TargetRoleType;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.exercisemuscletarget.repository.ExerciseMuscleTargetJpaRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.exerciseEntity;
import static com.ironcore.infrastructure.persistence.exercise.ExerciseEntityTestFactory.invalidExerciseEntity;
import static com.ironcore.infrastructure.persistence.exercisemuscletarget.ExerciseMuscleTargetEntityTestFactory.exerciseMuscleTargetEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExerciseByIdAdapterTest {

    @Mock
    private ExerciseJpaRepository exerciseJpaRepository;

    @Mock
    private ExerciseMuscleTargetJpaRepository exerciseMuscleTargetJpaRepository;

    @InjectMocks
    private GetExerciseByIdAdapter adapter;

    @Nested
    class FindActiveDetailById {

        @Test
        void shouldReturnProjectedExerciseDetailById() {
            when(exerciseJpaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(exerciseEntity()));
            when(exerciseMuscleTargetJpaRepository
                    .findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(1L))
                    .thenReturn(List.of(exerciseMuscleTargetEntity()));

            Optional<GetExerciseByIdResult> result = adapter.findActiveDetailById(new ExerciseId(1L));

            verify(exerciseJpaRepository).findByIdAndActiveTrue(1L);
            verify(exerciseMuscleTargetJpaRepository)
                    .findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(1L);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(new ExerciseId(1L));
            assertThat(result.get().name()).isEqualTo("Supino reto");
            assertThat(result.get().equipmentType().id()).isEqualTo(new EquipmentTypeId(1L));
            assertThat(result.get().equipmentType().code()).isEqualTo(new EquipmentTypeCode("CABLE"));
            assertThat(result.get().equipmentType().name()).isEqualTo("Cabo");
            assertThat(result.get().activityType().id()).isEqualTo(new ActivityTypeId(1L));
            assertThat(result.get().activityType().code()).isEqualTo(new ActivityTypeCode("STRENGTH"));
            assertThat(result.get().activityType().name()).isEqualTo("Força");
            assertThat(result.get().unilateral()).isFalse();
            assertThat(result.get().compound()).isTrue();
            assertThat(result.get().suggestedRestSeconds()).isEqualTo(90);
            assertThat(result.get().active()).isTrue();
            assertThat(result.get().muscleTargets()).singleElement().satisfies(muscleTarget -> {
                assertThat(muscleTarget.muscleSubgroup().id()).isEqualTo(new MuscleSubgroupId(1L));
                assertThat(muscleTarget.muscleSubgroup().code()).isEqualTo(new MuscleSubgroupCode("DELTOID"));
                assertThat(muscleTarget.muscleSubgroup().muscleGroupId()).isEqualTo(new MuscleGroupId(1L));
                assertThat(muscleTarget.muscleSubgroup().name()).isEqualTo("Deltoide");
                assertThat(muscleTarget.targetRole()).isEqualTo(TargetRoleType.PRIMARY);
            });
        }

        @Test
        void shouldReturnEmptyWhenActiveExerciseDoesNotExist() {
            when(exerciseJpaRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

            Optional<GetExerciseByIdResult> result = adapter.findActiveDetailById(new ExerciseId(99L));

            verify(exerciseJpaRepository).findByIdAndActiveTrue(99L);
            verifyNoInteractions(exerciseMuscleTargetJpaRepository);
            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapExerciseRepositoryFailure() {
            when(exerciseJpaRepository.findByIdAndActiveTrue(1L))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findActiveDetailById(new ExerciseId(1L)))
                    .withMessage("Falha ao buscar detalhe do exercício por id.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMuscleTargetRepositoryFailure() {
            when(exerciseJpaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(exerciseEntity()));
            when(exerciseMuscleTargetJpaRepository
                    .findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findActiveDetailById(new ExerciseId(1L)))
                    .withMessage("Falha ao buscar detalhe do exercício por id.")
                    .withCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldWrapMappingFailure() {
            when(exerciseJpaRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(invalidExerciseEntity()));
            when(exerciseMuscleTargetJpaRepository
                    .findAllByExercise_IdAndActiveTrueOrderByTargetRoleAscMuscleSubgroup_DisplayNameAsc(1L))
                    .thenReturn(List.of(exerciseMuscleTargetEntity()));

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findActiveDetailById(new ExerciseId(1L)))
                    .withMessage("Falha ao projetar detalhe do exercício por id.");
        }
    }
}
