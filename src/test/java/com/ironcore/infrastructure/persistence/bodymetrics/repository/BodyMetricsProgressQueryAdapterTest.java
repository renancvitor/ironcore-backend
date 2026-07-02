package com.ironcore.infrastructure.persistence.bodymetrics.repository;

import com.ironcore.application.bodymetrics.progress.BodyMetricsProgressProjection;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BodyMetricsProgressQueryAdapterTest {

    @Mock
    private BodyMetricsProgressJpaRepository bodyMetricsProgressJpaRepository;

    private BodyMetricsProgressQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BodyMetricsProgressQueryAdapter(bodyMetricsProgressJpaRepository);
    }

    @Nested
    class SuccessfulFindProgressData {

        @Test
        void shouldFindProgressDataByUserIdAndPeriod() {
            UserId userId = new UserId(1L);
            LocalDateTime startDate = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 6, 30, 23, 59);
            List<BodyMetricsProgressProjection> expectedProgress = List.of(
                    new BodyMetricsProgressProjection(
                            LocalDateTime.of(2026, 6, 1, 10, 0),
                            80.0,
                            20.0,
                            60.0,
                            15.0,
                            25.0,
                            39.0,
                            104.0,
                            118.0,
                            33.0,
                            27.0,
                            79.0,
                            93.0,
                            55.0,
                            36.0
                    )
            );

            when(bodyMetricsProgressJpaRepository.findProgressData(
                    userId.value(),
                    startDate,
                    endDate
            )).thenReturn(expectedProgress);

            List<BodyMetricsProgressProjection> result = adapter.findProgressData(
                    userId,
                    startDate,
                    endDate
            );

            verify(bodyMetricsProgressJpaRepository).findProgressData(
                    userId.value(),
                    startDate,
                    endDate
            );

            assertThat(result).isEqualTo(expectedProgress);
        }
    }

    @Nested
    class PersistenceFailure {

        @Test
        void shouldWrapRepositoryFailureInPersistenceException() {
            UserId userId = new UserId(1L);
            LocalDateTime startDate = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 6, 30, 23, 59);

            when(bodyMetricsProgressJpaRepository.findProgressData(
                    userId.value(),
                    startDate,
                    endDate
            )).thenThrow(new RuntimeException("database unavailable"));

            assertThatExceptionOfType(PersistenceException.class)
                    .isThrownBy(() -> adapter.findProgressData(
                            userId,
                            startDate,
                            endDate
                    ))
                    .withMessage("Falha ao buscar dados de progresso de métricas corporais.")
                    .withCauseInstanceOf(RuntimeException.class);

            verify(bodyMetricsProgressJpaRepository).findProgressData(
                    userId.value(),
                    startDate,
                    endDate
            );
        }

    }
}
