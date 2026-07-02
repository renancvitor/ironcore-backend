package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.bodymetrics.progress.*;
import com.ironcore.application.exception.BusinessRuleViolationException;
import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.bodymetrics.port.BodyMetricsProgressQueryPort;
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
        void shouldReturnBodyCompositionChartSeriesGroupedByMonthWithLastValidValue() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 3, 31);
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    startDate,
                    endDate
            );

            List<BodyMetricsProgressProjection> progress = List.of(
                    progressProjection(LocalDateTime.of(2026, 1, 5, 10, 0), 80.0, 20.0, 60.0),
                    progressProjection(LocalDateTime.of(2026, 1, 20, 10, 0), 79.0, 19.0, 61.0),
                    progressProjection(LocalDateTime.of(2026, 2, 10, 10, 0), 0.0, null, -1.0),
                    progressProjection(LocalDateTime.of(2026, 3, 15, 10, 0), 77.0, 18.0, 62.0)
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
            assertThat(weightSeries.points().getFirst().period()).isEqualTo("2026-01");
            assertThat(weightSeries.points().getFirst().value()).isEqualTo(79.0);
            assertThat(weightSeries.points().getLast().period()).isEqualTo("2026-03");
            assertThat(weightSeries.points().getLast().value()).isEqualTo(77.0);
            assertThat(weightSeries.points())
                    .extracting(BodyMetricsProgressPointResult::period)
                    .doesNotContain("2026-02");

            BodyMetricsProgressSeriesResult fatMassSeries = result.series().get(1);
            assertThat(fatMassSeries.metric()).isEqualTo(BodyMetricsProgressMetric.FAT_MASS_KG);
            assertThat(fatMassSeries.points()).hasSize(2);
            assertThat(fatMassSeries.points().getFirst().period()).isEqualTo("2026-01");
            assertThat(fatMassSeries.points().getFirst().value()).isEqualTo(19.0);
            assertThat(fatMassSeries.points().getLast().period()).isEqualTo("2026-03");
            assertThat(fatMassSeries.points().getLast().value()).isEqualTo(18.0);

            BodyMetricsProgressSeriesResult leanMassSeries = result.series().getLast();
            assertThat(leanMassSeries.metric()).isEqualTo(BodyMetricsProgressMetric.LEAN_MASS_KG);
            assertThat(leanMassSeries.points()).hasSize(2);
            assertThat(leanMassSeries.points().getFirst().period()).isEqualTo("2026-01");
            assertThat(leanMassSeries.points().getFirst().value()).isEqualTo(61.0);
            assertThat(leanMassSeries.points().getLast().period()).isEqualTo("2026-03");
            assertThat(leanMassSeries.points().getLast().value()).isEqualTo(62.0);
        }

        @Test
        void shouldNotReturnEmptySeriesWhenUserHasNoProgressData() {
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
            assertThat(result.series()).isEmpty();
        }

        @Test
        void shouldReturnOnlyMetricsThatBelongToChartType() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_FAT,
                    startDate,
                    endDate
            );

            List<BodyMetricsProgressProjection> progress = List.of(
                    progressProjectionWithBodyFatAndBmi(
                            LocalDateTime.of(2026, 6, 1, 10, 0),
                            15.0,
                            25.0
                    )
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(queryPort.findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            )).thenReturn(progress);

            GetBodyMetricsProgressChartResult result = getBodyMetricsProgressChartUseCase.execute(command);

            assertThat(result.series()).hasSize(1);
            assertThat(result.series().getFirst().metric()).isEqualTo(BodyMetricsProgressMetric.BODY_FAT_PERCENTAGE);
            assertThat(result.series().getFirst().points()).hasSize(1);
            assertThat(result.series().getFirst().points().getFirst().period()).isEqualTo("2026-06");
            assertThat(result.series().getFirst().points().getFirst().value()).isEqualTo(15.0);
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

        @Test
        void shouldFailWhenPeriodExceedsTwelveMonths() {
            User user = activeUser();
            BodyMetricsProgressChartCommand command = new BodyMetricsProgressChartCommand(
                    user.getId(),
                    BodyMetricsProgressChartType.BODY_COMPOSITION,
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2026, 1, 1)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChartUseCase.execute(command))
                    .withMessage("Período máximo permitido é de 12 meses.");

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

    private static BodyMetricsProgressProjection progressProjectionWithBodyFatAndBmi(
            LocalDateTime measuredAt,
            Double bodyFatPercentage,
            Double bmi
    ) {
        return new BodyMetricsProgressProjection(
                measuredAt,
                null,
                null,
                null,
                bodyFatPercentage,
                bmi,
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
