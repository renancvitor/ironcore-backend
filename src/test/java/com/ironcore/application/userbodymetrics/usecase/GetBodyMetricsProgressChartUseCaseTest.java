package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.userbodymetrics.port.BodyMetricsProgressQueryPort;
import com.ironcore.application.userbodymetrics.progress.*;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBodyMetricsProgressChartUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMetricsProgressQueryPort queryPort;

    @InjectMocks
    private GetBodyMetricsProgressChartUseCase getBodyMetricsProgressChartUseCase;

    @Nested
    class SuccessfulGetChart {

        @Test
        void shouldReturnBodyCompositionChartSeries() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    startDate,
                    endDate
            );

            List<BodyMetricsProgressProjection> progress = List.of(
                    progressProjection(LocalDateTime.of(2026, 6, 1, 10, 0), 80.0, 20.0, 60.0),
                    progressProjection(LocalDateTime.of(2026, 6, 15, 10, 0), 0.0, null, 61.0),
                    progressProjection(LocalDateTime.of(2026, 6, 30, 10, 0), 78.0, 18.0, 60.0)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(queryPort.findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            )).thenReturn(progress);

            GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(queryPort).findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );

            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);
            assertThat(result.chartType()).isEqualTo(BodyMetricsProgressChartType.BODY_COMPOSITION);
            assertThat(result.series()).hasSize(3);

            BodyMetricsProgressSeriesResult weightSeries = result.series().getFirst();
            assertThat(weightSeries.metric()).isEqualTo(BodyMetricsProgressMetric.WEIGHT_KG);
            assertThat(weightSeries.label()).isEqualTo("Peso");
            assertThat(weightSeries.unit()).isEqualTo("kg");
            assertThat(weightSeries.points()).hasSize(2);
            assertThat(weightSeries.points().getFirst().period()).isEqualTo("2026-06-01");
            assertThat(weightSeries.points().getFirst().value()).isEqualTo(80.0);
            assertThat(weightSeries.points().getLast().period()).isEqualTo("2026-06-30");
            assertThat(weightSeries.points().getLast().value()).isEqualTo(78.0);

            BodyMetricsProgressSeriesResult fatMassSeries = result.series().get(1);
            assertThat(fatMassSeries.metric()).isEqualTo(BodyMetricsProgressMetric.FAT_MASS_KG);
            assertThat(fatMassSeries.points()).hasSize(2);

            BodyMetricsProgressSeriesResult leanMassSeries = result.series().getLast();
            assertThat(leanMassSeries.metric()).isEqualTo(BodyMetricsProgressMetric.LEAN_MASS_KG);
            assertThat(leanMassSeries.points()).hasSize(3);
        }

        @Test
        void shouldReturnSeriesWithEmptyPointsWhenUserHasNoProgressData() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_FAT,
                    startDate,
                    endDate
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(queryPort.findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            )).thenReturn(List.of());

            GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(queryPort).findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );

            assertThat(result.chartType()).isEqualTo(BodyMetricsProgressChartType.BODY_FAT);
            assertThat(result.series()).hasSize(1);
            assertThat(result.series().getFirst().metric()).isEqualTo(BodyMetricsProgressMetric.BODY_FAT_PERCENTAGE);
            assertThat(result.series().getFirst().points()).isEmpty();
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    new UserId(1L),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenChartTypeIsNull() {
            User user = activeUser();
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    null,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(BusinessRuleViolationException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("Tipo do gráfico é obrigatório.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }

        @Test
        void shouldFailWhenDatesAreNull() {
            User user = activeUser();
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    null,
                    null
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(BusinessRuleViolationException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("As datas são obrigatórias.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }

        @Test
        void shouldFailWhenStartDateIsAfterEndDate() {
            User user = activeUser();
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    LocalDate.of(2026, 6, 30),
                    LocalDate.of(2026, 6, 1)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("Data inicial não pode ser maior do que data final.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }
    }

    private static BodyMetricsProgressProjection progressProjection(
            LocalDateTime measuredAt,
            Double weightKg,
            Double fatMassKg,
            Double leanMassKg
    ) {
        return new BodyMetricsProgressProjection(
                measuredAt,
                weightKg,
                fatMassKg,
                leanMassKg,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
