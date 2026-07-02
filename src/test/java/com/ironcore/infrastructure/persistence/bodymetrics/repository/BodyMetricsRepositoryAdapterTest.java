package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
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
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;
import static com.ironcore.infrastructure.persistence.bodymetrics.BodyMetricsTestFactory.createUserBodyMetricsEntity;
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
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private BodyMetricsRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveUserBodyMetrics() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(bodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenReturn(createUserBodyMetricsEntity());

            BodyMetrics result = adapter.save(restoreBodyMetrics());

            assertThat(result.getId()).isEqualTo(new BodyMetricsId(1L));
            assertThat(result.getUserId()).isEqualTo(new UserId(1L));
            verify(userJpaRepository).getReferenceById(1L);
            verify(bodyMetricsJpaRepository).save(anyUserBodyMetricsEntity());
        }

        @Test
        void shouldWrapMappingFailureBeforePersistence() {
            when(userJpaRepository.getReferenceById(1L)).thenThrow(new RuntimeException("user unavailable"));
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao converter domínio para entidade.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(bodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(bodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenReturn(invalidUserBodyMetricsEntity());
            BodyMetrics bodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(bodyMetrics))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindUserBodyMetricsById() {
            when(bodyMetricsJpaRepository.findById(1L)).thenReturn(Optional.of(createUserBodyMetricsEntity()));

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
    class FindByIdAndUserId {

        @Test
        void shouldFindUserBodyMetricsByIdAndUserId() {
            when(bodyMetricsJpaRepository.findByIdAndUser_Id(1L, 1L))
                    .thenReturn(Optional.of(createUserBodyMetricsEntity()));

            Optional<BodyMetrics> result = adapter.findByIdAndUserId(
                    new BodyMetricsId(1L),
                    new UserId(1L)
            );

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new BodyMetricsId(1L));
            assertThat(result.get().getUserId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyWhenUserBodyMetricsDoesNotBelongToUser() {
            when(bodyMetricsJpaRepository.findByIdAndUser_Id(1L, 99L)).thenReturn(Optional.empty());

            Optional<BodyMetrics> result = adapter.findByIdAndUserId(
                    new BodyMetricsId(1L),
                    new UserId(99L)
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(bodyMetricsJpaRepository.findByIdAndUser_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndUserId(bodyMetricsId, userId))
                    .withMessage("Falha ao buscar métricas corporais por id e usuário.");
        }
    }

    @Nested
    class FindLatestByUserId {

        @Test
        void shouldFindLatestUserBodyMetricsByUserId() {
            when(bodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenReturn(Optional.of(createUserBodyMetricsEntity()));

            Optional<BodyMetrics> result = adapter.findLatestByUserId(new UserId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyWhenUserDoesNotHaveBodyMetrics() {
            when(bodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(99L))
                    .thenReturn(Optional.empty());

            Optional<BodyMetrics> result = adapter.findLatestByUserId(new UserId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(bodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findLatestByUserId(userId))
                    .withMessage("Falha ao buscar último registro pelo usuário.");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteUserBodyMetricsById() {
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
    private BodyMetricsEntity anyUserBodyMetricsEntity() {
        return notNull(BodyMetricsEntity.class);
    }

    private BodyMetricsEntity invalidUserBodyMetricsEntity() {
        BodyMetricsEntity entity = createUserBodyMetricsEntity();
        entity.setId(null);
        return entity;
    }
}
