package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.person.repository.PersonJpaRepository;
import com.ironcore.infrastructure.persistence.bodymetrics.entity.BodyMetricsEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.util.Optional;

import static com.ironcore.domain.bodymetrics.BodyMetricsTestFactory.restoreBodyMetrics;
import static com.ironcore.infrastructure.persistence.person.PersonEntityTestFactory.personEntity;
import static com.ironcore.infrastructure.persistence.bodymetrics.BodyMetricsTestFactory.createPersonBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BodyMetricsRepositoryAdapterTest {

    @Mock
    private BodyMetricsJpaRepository bodyMetricsJpaRepository;

    @Mock
    private PersonJpaRepository personJpaRepository;

    @InjectMocks
    private BodyMetricsRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveBodyMetrics() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(bodyMetricsJpaRepository.save(anyBodyMetricsEntity()))
                    .thenReturn(createPersonBodyMetricsEntity());

            BodyMetrics result = adapter.save(restoreBodyMetrics());

            assertThat(result.getId()).isEqualTo(new BodyMetricsId(1L));
            assertThat(result.getPersonId()).isEqualTo(new PersonId(1L));
            verify(personJpaRepository).getReferenceById(1L);
            verify(bodyMetricsJpaRepository).save(anyBodyMetricsEntity());
        }

        @Test
        void shouldWrapMappingFailureBeforePersistence() {
            when(personJpaRepository.getReferenceById(1L)).thenThrow(new RuntimeException("user unavailable"));
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao converter domínio para entidade.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(bodyMetricsJpaRepository.save(anyBodyMetricsEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(personJpaRepository.getReferenceById(1L)).thenReturn(personEntity());
            when(bodyMetricsJpaRepository.save(anyBodyMetricsEntity()))
                    .thenReturn(invalidBodyMetricsEntity());
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindBodyMetricsById() {
            when(bodyMetricsJpaRepository.findById(1L)).thenReturn(Optional.of(createPersonBodyMetricsEntity()));

            Optional<BodyMetrics> result = adapter.findById(new BodyMetricsId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new BodyMetricsId(1L));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(bodyMetricsJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<BodyMetrics> result = adapter.findById(new BodyMetricsId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(bodyMetricsJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(bodyMetricsId))
                    .withMessage("Falha ao buscar métricas corporais por id.");
        }
    }

    @Nested
    class FindByIdAndPersonId {

        @Test
        void shouldFindBodyMetricsByIdAndPersonId() {
            when(bodyMetricsJpaRepository.findByIdAndPerson_Id(1L, 1L))
                    .thenReturn(Optional.of(createPersonBodyMetricsEntity()));

            Optional<BodyMetrics> result = adapter.findByIdAndPersonId(
                    new BodyMetricsId(1L),
                    new PersonId(1L)
            );

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new BodyMetricsId(1L));
            assertThat(result.get().getPersonId()).isEqualTo(new PersonId(1L));
        }

        @Test
        void shouldReturnEmptyWhenBodyMetricsDoesNotBelongToPerson() {
            when(bodyMetricsJpaRepository.findByIdAndPerson_Id(1L, 99L)).thenReturn(Optional.empty());

            Optional<BodyMetrics> result = adapter.findByIdAndPersonId(
                    new BodyMetricsId(1L),
                    new PersonId(99L)
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(bodyMetricsJpaRepository.findByIdAndPerson_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            PersonId userId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndPersonId(bodyMetricsId, userId))
                    .withMessage("Falha ao buscar métricas corporais por id e pessoa.");
        }
    }

    @Nested
    class FindLatestByPersonId {

        @Test
        void shouldFindLatestBodyMetricsByPersonId() {
            when(bodyMetricsJpaRepository.findFirstByPerson_IdOrderByMeasuredAtDesc(1L))
                    .thenReturn(Optional.of(createPersonBodyMetricsEntity()));

            Optional<BodyMetrics> result = adapter.findLatestByPersonId(new PersonId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getPersonId()).isEqualTo(new PersonId(1L));
        }

        @Test
        void shouldReturnEmptyWhenPersonDoesNotHaveBodyMetrics() {
            when(bodyMetricsJpaRepository.findFirstByPerson_IdOrderByMeasuredAtDesc(99L))
                    .thenReturn(Optional.empty());

            Optional<BodyMetrics> result = adapter.findLatestByPersonId(new PersonId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(bodyMetricsJpaRepository.findFirstByPerson_IdOrderByMeasuredAtDesc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            PersonId userId = new PersonId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findLatestByPersonId(userId))
                    .withMessage("Falha ao buscar último registro pela pessoa.");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteBodyMetricsById() {
            adapter.deleteById(new BodyMetricsId(1L));

            verify(bodyMetricsJpaRepository).deleteById(1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            doThrow(new RuntimeException("database unavailable")).when(bodyMetricsJpaRepository).deleteById(1L);
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.deleteById(bodyMetricsId))
                    .withMessage("Falha ao excluir métricas corporais por id.");
        }
    }

    @NonNull
    private BodyMetricsEntity anyBodyMetricsEntity() {
        return notNull(BodyMetricsEntity.class);
    }

    private BodyMetricsEntity invalidBodyMetricsEntity() {
        BodyMetricsEntity entity = createPersonBodyMetricsEntity();
        entity.setId(null);
        return entity;
    }
}
