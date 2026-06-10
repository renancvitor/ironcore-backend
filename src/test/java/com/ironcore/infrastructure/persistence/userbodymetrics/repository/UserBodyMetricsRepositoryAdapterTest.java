package com.ironcore.infrastructure.persistence.userbodymetrics.repository;

import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.exception.PersistenceException;
import com.ironcore.infrastructure.persistence.user.repository.UserJpaRepository;
import com.ironcore.infrastructure.persistence.userbodymetrics.entity.UserBodyMetricsEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.userbodymetrics.UserBodyMetricsTestFactory.restoreBodyMetrics;
import static com.ironcore.infrastructure.persistence.user.UserEntityTestFactory.userEntity;
import static com.ironcore.infrastructure.persistence.userbodymetrics.UserBodyMetricsTestFactory.createUserBodyMetricsEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBodyMetricsRepositoryAdapterTest {

    @Mock
    private UserBodyMetricsJpaRepository userBodyMetricsJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserBodyMetricsRepositoryAdapter adapter;

    @Nested
    class Save {

        @Test
        void shouldSaveUserBodyMetrics() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(userBodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenReturn(createUserBodyMetricsEntity());

            UserBodyMetrics result = adapter.save(restoreBodyMetrics());

            assertThat(result.getId()).isEqualTo(new UserBodyMetricsId(1L));
            assertThat(result.getUserId()).isEqualTo(new UserId(1L));
            verify(userJpaRepository).getReferenceById(1L);
            verify(userBodyMetricsJpaRepository).save(anyUserBodyMetricsEntity());
        }

        @Test
        void shouldWrapMappingFailureBeforePersistence() {
            when(userJpaRepository.getReferenceById(1L)).thenThrow(new RuntimeException("user unavailable"));
            UserBodyMetrics userBodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(userBodyMetrics))
                    .withMessage("Falha ao converter domínio para entidade.");
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(userBodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenThrow(new RuntimeException("database unavailable"));
            UserBodyMetrics userBodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.save(userBodyMetrics))
                    .withMessage("Falha ao persistir entidade.");
        }

        @Test
        void shouldWrapMappingFailureAfterPersistence() {
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity());
            when(userBodyMetricsJpaRepository.save(anyUserBodyMetricsEntity()))
                    .thenReturn(invalidUserBodyMetricsEntity());
            UserBodyMetrics userBodyMetrics = restoreBodyMetrics();

            assertThatExceptionOfType(DataMappingException.class)
                    .isThrownBy(() -> adapter.save(userBodyMetrics))
                    .withMessage("Falha ao converter persistido para domain.");
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindUserBodyMetricsById() {
            when(userBodyMetricsJpaRepository.findById(1L)).thenReturn(Optional.of(createUserBodyMetricsEntity()));

            Optional<UserBodyMetrics> result = adapter.findById(new UserBodyMetricsId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new UserBodyMetricsId(1L));
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(userBodyMetricsJpaRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<UserBodyMetrics> result = adapter.findById(new UserBodyMetricsId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userBodyMetricsJpaRepository.findById(1L)).thenThrow(new RuntimeException("database unavailable"));
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findById(userBodyMetricsId))
                    .withMessage("Falha ao buscar métricas corporais por id.");
        }
    }

    @Nested
    class FindByIdAndUserId {

        @Test
        void shouldFindUserBodyMetricsByIdAndUserId() {
            when(userBodyMetricsJpaRepository.findByIdAndUser_Id(1L, 1L))
                    .thenReturn(Optional.of(createUserBodyMetricsEntity()));

            Optional<UserBodyMetrics> result = adapter.findByIdAndUserId(
                    new UserBodyMetricsId(1L),
                    new UserId(1L)
            );

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(new UserBodyMetricsId(1L));
            assertThat(result.get().getUserId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyWhenUserBodyMetricsDoesNotBelongToUser() {
            when(userBodyMetricsJpaRepository.findByIdAndUser_Id(1L, 99L)).thenReturn(Optional.empty());

            Optional<UserBodyMetrics> result = adapter.findByIdAndUserId(
                    new UserBodyMetricsId(1L),
                    new UserId(99L)
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userBodyMetricsJpaRepository.findByIdAndUser_Id(1L, 1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByIdAndUserId(userBodyMetricsId, userId))
                    .withMessage("Falha ao buscar métricas corporais por id e usuário.");
        }
    }

    @Nested
    class FindLatestByUserId {

        @Test
        void shouldFindLatestUserBodyMetricsByUserId() {
            when(userBodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenReturn(Optional.of(createUserBodyMetricsEntity()));

            Optional<UserBodyMetrics> result = adapter.findLatestByUserId(new UserId(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyWhenUserDoesNotHaveBodyMetrics() {
            when(userBodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(99L))
                    .thenReturn(Optional.empty());

            Optional<UserBodyMetrics> result = adapter.findLatestByUserId(new UserId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userBodyMetricsJpaRepository.findFirstByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findLatestByUserId(userId))
                    .withMessage("Falha ao buscar último registro pelo usuário.");
        }
    }

    @Nested
    class FindByUserIdOrderByMeasuredAtDesc {

        @Test
        void shouldFindUserBodyMetricsByUserIdOrderedByMeasuredAtDesc() {
            when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenReturn(List.of(createUserBodyMetricsEntity()));

            List<UserBodyMetrics> result = adapter.findByUserIdOrderByMeasuredAtDesc(new UserId(1L));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUserId()).isEqualTo(new UserId(1L));
        }

        @Test
        void shouldReturnEmptyListWhenUserDoesNotHaveBodyMetrics() {
            when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDesc(99L)).thenReturn(List.of());

            List<UserBodyMetrics> result = adapter.findByUserIdOrderByMeasuredAtDesc(new UserId(99L));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldWrapRepositoryFailure() {
            when(userBodyMetricsJpaRepository.findByUser_IdOrderByMeasuredAtDesc(1L))
                    .thenThrow(new RuntimeException("database unavailable"));
            UserId userId = new UserId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findByUserIdOrderByMeasuredAtDesc(userId))
                    .withMessage("Falha ao buscar registros pelo usuário com data de medição descendente.");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteUserBodyMetricsById() {
            adapter.deleteById(new UserBodyMetricsId(1L));

            verify(userBodyMetricsJpaRepository).deleteById(1L);
        }

        @Test
        void shouldWrapRepositoryFailure() {
            doThrow(new RuntimeException("database unavailable")).when(userBodyMetricsJpaRepository).deleteById(1L);
            UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.deleteById(userBodyMetricsId))
                    .withMessage("Falha ao excluir métricas corporais por id.");
        }
    }

    @NonNull
    private UserBodyMetricsEntity anyUserBodyMetricsEntity() {
        return notNull(UserBodyMetricsEntity.class);
    }

    private UserBodyMetricsEntity invalidUserBodyMetricsEntity() {
        UserBodyMetricsEntity entity = createUserBodyMetricsEntity();
        entity.setId(null);
        return entity;
    }
}
