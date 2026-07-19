package com.ironcore.infrastructure.persistence.muscle.musclegroup.repository;

import com.ironcore.domain.muscle.musclegroup.model.MuscleGroup;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupCode;
import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.invalidMuscleGroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclegroup.MuscleGroupEntityTestFactory.muscleGroupEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MuscleGroupAdapterTest {

    @Mock
    private MuscleGroupJpaRepository muscleGroupJpaRepository;

    @InjectMocks
    private MuscleGroupAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindMuscleGroupById() {
            when(muscleGroupJpaRepository.findById(1L)).thenReturn(Optional.of(muscleGroupEntity()));

            Optional<MuscleGroup> result = adapter.findById(new MuscleGroupId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(result.get().getCode()).isEqualTo(new MuscleGroupCode("BACK"));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(muscleGroupJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<MuscleGroup> result = adapter.findById(new MuscleGroupId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleGroupJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            MuscleGroupId muscleGroupId = new MuscleGroupId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(muscleGroupId))
                    .withMessage("Falha ao buscar muscle group por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(muscleGroupJpaRepository.findById(1L)).thenReturn(Optional.of(invalidMuscleGroupEntity()));
            MuscleGroupId muscleGroupId = new MuscleGroupId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(muscleGroupId))
                    .withMessage("Falha ao converter grupo muscular de entidade para domínio.");
        }
    }

    @Nested
    class FindByCode {

        @Test
        void shouldFindMuscleGroupByCode() {
            when(muscleGroupJpaRepository.findByCode("BACK")).thenReturn(Optional.of(muscleGroupEntity()));

            Optional<MuscleGroup> result = adapter.findByCode(new MuscleGroupCode(" back "));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(result.get().getCode()).isEqualTo(new MuscleGroupCode("BACK"));
        }

        @Test
        void shouldReturnEmptyWhenCodeDoesNotExist() {
            when(muscleGroupJpaRepository.findByCode("MISSING")).thenReturn(Optional.empty());

            Optional<MuscleGroup> result = adapter.findByCode(new MuscleGroupCode("missing"));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleGroupJpaRepository.findByCode("BACK"))
                    .thenThrow(new RuntimeException("database unavailable"));
            MuscleGroupCode code = new MuscleGroupCode("BACK");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByCode(code))
                    .withMessage("Falha ao buscar muscle group por code.");
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldFindAllMuscleGroups() {
            when(muscleGroupJpaRepository.findAll()).thenReturn(List.of(muscleGroupEntity()));

            List<MuscleGroup> result = adapter.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(result.getFirst().getCode()).isEqualTo(new MuscleGroupCode("BACK"));
        }

        @Test
        void shouldReturnEmptyWhenNoMuscleGroupsExist() {
            when(muscleGroupJpaRepository.findAll()).thenReturn(List.of());

            List<MuscleGroup> result = adapter.findAll();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleGroupJpaRepository.findAll()).thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findAll())
                    .withMessage("Falha ao buscar muscle groups.");
        }
    }
}
