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
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBodyMetricsProgressChangesUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMetricsProgressQueryPort queryPort;

    @InjectMocks
    private GetBodyMetricsProgressChangesUseCase getBodyMetricsProgressChangesUseCase;

    @Nested
    class SuccessfulGetChanges {

        @Test
        void shouldReturnProgressChangesForEnabledMetrics() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    user.getId(),
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

            GetBodyMetricsProgressChangesResult result = getBodyMetricsProgressChangesUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(queryPort).findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );

            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);
            assertThat(result.changes()).hasSize(3);

            BodyMetricsProgressChangeResult weightChange = result.changes().getFirst();
            assertThat(weightChange.metric()).isEqualTo(BodyMetricsProgressMetric.WEIGHT_KG);
            assertThat(weightChange.label()).isEqualTo("Peso");
            assertThat(weightChange.unit()).isEqualTo("kg");
            assertThat(weightChange.firstDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(weightChange.firstValue()).isEqualTo(80.0);
            assertThat(weightChange.lastDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(weightChange.lastValue()).isEqualTo(78.0);
            assertThat(weightChange.absoluteChange()).isEqualTo(-2.0);
            assertThat(weightChange.percentageChange()).isCloseTo(-2.5, within(0.0001));

            BodyMetricsProgressChangeResult fatMassChange = result.changes().get(1);
            assertThat(fatMassChange.metric()).isEqualTo(BodyMetricsProgressMetric.FAT_MASS_KG);
            assertThat(fatMassChange.absoluteChange()).isEqualTo(-2.0);
            assertThat(fatMassChange.percentageChange()).isCloseTo(-10.0, within(0.0001));

            BodyMetricsProgressChangeResult leanMassChange = result.changes().getLast();
            assertThat(leanMassChange.metric()).isEqualTo(BodyMetricsProgressMetric.LEAN_MASS_KG);
            assertThat(leanMassChange.absoluteChange()).isEqualTo(0.0);
            assertThat(leanMassChange.percentageChange()).isCloseTo(0.0, within(0.0001));
        }

        @Test
        void shouldIgnoreMetricsWithoutAtLeastTwoValidPoints() {
            User user = activeUser();
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    user.getId(),
                    startDate,
                    endDate
            );

            List<BodyMetricsProgressProjection> progress = List.of(
                    progressProjection(LocalDateTime.of(2026, 6, 1, 10, 0), 80.0, null, null),
                    progressProjection(LocalDateTime.of(2026, 6, 30, 10, 0), 78.0, null, null)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(queryPort.findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            )).thenReturn(progress);

            GetBodyMetricsProgressChangesResult result = getBodyMetricsProgressChangesUseCase.execute(command);

            verify(userRepository).findById(command.userId());
            verify(queryPort).findProgressData(
                    command.userId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );

            assertThat(result.changes()).hasSize(1);
            assertThat(result.changes().getFirst().metric()).isEqualTo(BodyMetricsProgressMetric.WEIGHT_KG);
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    new UserId(1L),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChangesUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    user.getId(),
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 6, 30)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChangesUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }
    }

    @Nested
    class RequestValidation {

        @Test
        void shouldFailWhenDatesAreNull() {
            User user = activeUser();
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    user.getId(),
                    null,
                    null
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(BusinessRuleViolationException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChangesUseCase.execute(command))
                    .withMessage("As datas são obrigatórias.");

            verify(userRepository).findById(command.userId());
            verifyNoInteractions(queryPort);
        }

        @Test
        void shouldFailWhenStartDateIsAfterEndDate() {
            User user = activeUser();
            BodyMetricsProgressChangesCommand command = new BodyMetricsProgressChangesCommand(
                    user.getId(),
                    LocalDate.of(2026, 6, 30),
                    LocalDate.of(2026, 6, 1)
            );

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> getBodyMetricsProgressChangesUseCase.execute(command))
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
