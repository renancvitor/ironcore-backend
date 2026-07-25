package com.ironcore.infrastructure.persistence.muscle.musclesubgroup.repository;

import com.ironcore.domain.muscle.musclegroup.valueobject.MuscleGroupId;
import com.ironcore.domain.muscle.musclesubgroup.model.MuscleSubgroup;
import com.ironcore.domain.muscle.musclesubgroup.valueobject.MuscleSubgroupCode;
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

import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.invalidMuscleSubgroupEntity;
import static com.ironcore.infrastructure.persistence.muscle.musclesubgroup.MuscleSubgroupEntityTestFactory.muscleSubgroupEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MuscleSubgroupAdapterTest {

    @Mock
    private MuscleSubgroupJpaRepository muscleSubgroupJpaRepository;

    @InjectMocks
    private MuscleSubgroupAdapter adapter;

    @Nested
    class FindById {

        @Test
        void shouldFindMuscleSubgroupById() {
            when(muscleSubgroupJpaRepository.findById(1L)).thenReturn(Optional.of(muscleSubgroupEntity()));

            Optional<MuscleSubgroup> result = adapter.findById(new MuscleSubgroupId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(result.get().getMuscleGroupId()).isEqualTo(new MuscleGroupId(1L));
            assertThat(result.get().getCode()).isEqualTo(new MuscleSubgroupCode("DELTOID"));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(muscleSubgroupJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<MuscleSubgroup> result = adapter.findById(new MuscleSubgroupId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleSubgroupJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            MuscleSubgroupId muscleSubgroupId = new MuscleSubgroupId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(muscleSubgroupId))
                    .withMessage("Falha ao buscar muscle subgroup por id.");
        }

        @Test
        void shouldWrapMappingFailure() {
            when(muscleSubgroupJpaRepository.findById(1L)).thenReturn(Optional.of(invalidMuscleSubgroupEntity()));
            MuscleSubgroupId muscleSubgroupId = new MuscleSubgroupId(1L);

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.findById(muscleSubgroupId))
                    .withMessage("Falha ao converter subgrupo muscular de entidade para domínio.");
        }
    }

    @Nested
    class FindByCode {

        @Test
        void shouldFindMuscleSubgroupByCode() {
            when(muscleSubgroupJpaRepository.findByCode("DELTOID")).thenReturn(Optional.of(muscleSubgroupEntity()));

            Optional<MuscleSubgroup> result = adapter.findByCode(new MuscleSubgroupCode(" deltoid "));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new MuscleSubgroupId(1L));
            assertThat(result.get().getCode()).isEqualTo(new MuscleSubgroupCode("DELTOID"));
        }

        @Test
        void shouldReturnEmptyWhenCodeDoesNotExist() {
            when(muscleSubgroupJpaRepository.findByCode("MISSING")).thenReturn(Optional.empty());

            Optional<MuscleSubgroup> result = adapter.findByCode(new MuscleSubgroupCode("missing"));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(muscleSubgroupJpaRepository.findByCode("DELTOID"))
                    .thenThrow(new RuntimeException("database unavailable"));
            MuscleSubgroupCode code = new MuscleSubgroupCode("DELTOID");

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByCode(code))
                    .withMessage("Falha ao buscar muscle subgroup por code.");
        }
    }

}
