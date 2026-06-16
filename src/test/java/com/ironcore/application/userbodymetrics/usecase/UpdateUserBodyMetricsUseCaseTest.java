package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
import com.ironcore.application.userbodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.update.UpdateUserBodyMetricsUseCase;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.user.enums.SexType;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.service.BMICalculator;
import com.ironcore.domain.userbodymetrics.service.FatMassCalculator;
import com.ironcore.domain.userbodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.userbodymetrics.service.NavyBodyFatCalculator;
import com.ironcore.domain.userbodymetrics.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithFemaleRequiredCircumferences;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithInsufficientFemaleCircumferences;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithInsufficientMaleCircumferences;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithMaleRequiredCircumferences;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithoutCircumferences;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithoutHeight;
import static com.ironcore.application.userbodymetrics.UpdateUserBodyMetricsUseCaseTestFactory.commandWithoutWeight;
import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserBodyMetricsUseCaseTest {

    private static final LocalDateTime EXISTING_MEASURED_AT = LocalDateTime.of(2026, 5, 1, 8, 0);
    private static final LocalDateTime EXISTING_UPDATED_AT = LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDateTime UPDATED_METRICS_AT = LocalDateTime.of(2026, 5, 23, 10, 0);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBodyMetricsRepository userBodyMetricsRepository;

    @Spy
    private BMICalculator bmiCalculator = new BMICalculator();

    @Spy
    private NavyBodyFatCalculator navyBodyFatCalculator = new NavyBodyFatCalculator();

    private BodyFatPercentageCalculator bodyFatPercentageCalculator;

    @Spy
    private FatMassCalculator fatMassCalculator = new FatMassCalculator();

    @Spy
    private LeanMassCalculator leanMassCalculator = new LeanMassCalculator();

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher auditLogPublisher;

    private UpdateUserBodyMetricsUseCase updateUserBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        bodyFatPercentageCalculator = new BodyFatPercentageCalculator(navyBodyFatCalculator);
        updateUserBodyMetricsUseCase = new UpdateUserBodyMetricsUseCase(
                userRepository,
                userBodyMetricsRepository,
                bmiCalculator,
                bodyFatPercentageCalculator,
                fatMassCalculator,
                leanMassCalculator,
                clock,
                auditLogPublisher
        );
    }

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdateUserBodyMetricsWithWeightAndHeight() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithoutCircumferences();
            double expectedBmi = command.weight().value() / Math.pow(command.height().inMeters(), 2);

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            UpdateUserBodyMetricsResult result = updateUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);
            ArgumentCaptor<LoggableData> auditBeforeStateCaptor = ArgumentCaptor.forClass(LoggableData.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.userBodyMetricsId().value()),
                    auditBeforeStateCaptor.capture(),
                    auditAfterStateCaptor.capture()
            );

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();
            UserBodyMetricsAuditData auditBeforeState = (UserBodyMetricsAuditData) auditBeforeStateCaptor.getValue();
            UserBodyMetricsAuditData auditAfterState = (UserBodyMetricsAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedUserBodyMetrics.getId()).isEqualTo(command.userBodyMetricsId());
            assertThat(savedUserBodyMetrics.getUserId()).isEqualTo(command.userId());
            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedUserBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedUserBodyMetrics.getWeight()).isEqualTo(command.weight());
            assertThat(savedUserBodyMetrics.getHeight()).isEqualTo(command.height());
            assertThat(savedUserBodyMetrics.getCircumferences()).isNull();
            assertThat(savedUserBodyMetrics.getBmi().value()).isCloseTo(expectedBmi, within(0.0001));
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getNotes()).isEqualTo(command.notes());

            assertThat(result.id()).isEqualTo(command.userBodyMetricsId());
            assertThat(result.userId()).isEqualTo(command.userId());
            assertThat(result.measuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(result.updatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(result.weight()).isEqualTo(command.weight());
            assertThat(result.height()).isEqualTo(command.height());
            assertThat(result.bmi()).isEqualTo(savedUserBodyMetrics.getBmi());

            assertThat(auditBeforeState.id()).isEqualTo(command.userBodyMetricsId().value());
            assertThat(auditBeforeState.userId()).isEqualTo(command.userId().value());
            assertThat(auditBeforeState.measuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(auditBeforeState.weightKg()).isEqualTo(80.0);
            assertThat(auditBeforeState.heightCm()).isEqualTo(180.0);
            assertThat(auditBeforeState.bmi()).isEqualTo(24.69);
            assertThat(auditBeforeState.notes()).isEqualTo("OLD TEXT");

            assertThat(auditAfterState.id()).isEqualTo(command.userBodyMetricsId().value());
            assertThat(auditAfterState.userId()).isEqualTo(command.userId().value());
            assertThat(auditAfterState.measuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(auditAfterState.weightKg()).isEqualTo(command.weight().value());
            assertThat(auditAfterState.heightCm()).isEqualTo(command.height().value());
            assertThat(auditAfterState.bmi()).isCloseTo(expectedBmi, within(0.0001));
            assertThat(auditAfterState.bodyFatPercentage()).isNull();
            assertThat(auditAfterState.fatMassKg()).isNull();
            assertThat(auditAfterState.leanMassKg()).isNull();
            assertThat(auditAfterState.notes()).isEqualTo(command.notes());
        }

        @Test
        void shouldCalculateBodyFatFatMassAndLeanMassForMaleUserWithSufficientCircumferences() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithMaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.MALE, command.height(), command.circumferences());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.userBodyMetricsId().value()),
                    any(UserBodyMetricsAuditData.class),
                    any(UserBodyMetricsAuditData.class)
            );

            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedUserBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldCalculateBodyFatFatMassAndLeanMassForFemaleUserWithSufficientCircumferences() {
            User user = activeFemaleUser();
            UpdateUserBodyMetricsCommand command = commandWithFemaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.FEMALE, command.height(), command.circumferences());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.userBodyMetricsId().value()),
                    any(UserBodyMetricsAuditData.class),
                    any(UserBodyMetricsAuditData.class)
            );

            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedUserBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesWhenCircumferencesAreInsufficient() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithInsufficientMaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedUserBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.userBodyMetricsId().value()),
                    any(UserBodyMetricsAuditData.class),
                    any(UserBodyMetricsAuditData.class)
            );
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesForFemaleUserWhenHipIsMissing() {
            User user = activeFemaleUser();
            UpdateUserBodyMetricsCommand command = commandWithInsufficientFemaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedUserBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.userBodyMetricsId().value()),
                    any(UserBodyMetricsAuditData.class),
                    any(UserBodyMetricsAuditData.class)
            );
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdateUserBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updateUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UpdateUserBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> updateUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class RequiredMeasurementsValidation {

        @Test
        void shouldFailWhenWeightIsMissing() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithoutWeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> updateUserBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenHeightIsMissing() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithoutHeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> updateUserBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(userBodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class UserBodyMetricsValidation {

        @Test
        void shouldFailWhenUserBodyMetricsDoesNotExistForUser() {
            User user = activeUser();
            UpdateUserBodyMetricsCommand command = commandWithoutCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            when(userBodyMetricsRepository.findByIdAndUserId(command.userBodyMetricsId(), command.userId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updateUserBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(userBodyMetricsRepository).findByIdAndUserId(command.userBodyMetricsId(), command.userId());
            verify(userBodyMetricsRepository, never()).save(any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    private void givenFixedClock() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-05-23T10:00:00Z"),
                ZoneOffset.UTC
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private UserBodyMetrics givenExistingUserBodyMetrics(UpdateUserBodyMetricsCommand command) {
        UserBodyMetrics existingUserBodyMetrics = UserBodyMetrics.restore(
                command.userBodyMetricsId(),
                command.userId(),
                EXISTING_MEASURED_AT,
                new BodyWeightKg(80.0),
                new BodyHeightCm(180.0),
                null,
                new BMI(24.69),
                null,
                null,
                null,
                EXISTING_UPDATED_AT,
                "OLD TEXT"
        );

        when(userBodyMetricsRepository.findByIdAndUserId(command.userBodyMetricsId(), command.userId()))
                .thenReturn(Optional.of(existingUserBodyMetrics));

        return existingUserBodyMetrics;
    }

    private void givenUpdatedUserBodyMetricsIsPersisted() {
        when(userBodyMetricsRepository.save(any(UserBodyMetrics.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User activeFemaleUser() {
        return User.restore(
                new UserId(1L),
                "Renata",
                email("renata@example.com"),
                passwordHash("hashed-password"),
                sex(SexType.FEMALE),
                false,
                true,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
