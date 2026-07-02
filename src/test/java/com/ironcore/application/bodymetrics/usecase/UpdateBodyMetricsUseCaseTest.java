package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.bodymetrics.BodyMetricsAuditData;
import com.ironcore.application.bodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsResult;
import com.ironcore.application.bodymetrics.update.UpdateBodyMetricsUseCase;
import com.ironcore.domain.bodymetrics.valueobject.BMI;
import com.ironcore.domain.bodymetrics.valueobject.BodyHeightCm;
import com.ironcore.domain.bodymetrics.valueobject.BodyWeightKg;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.Sex;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.bodymetrics.model.BodyMetrics;
import com.ironcore.domain.bodymetrics.repository.BodyMetricsRepository;
import com.ironcore.domain.bodymetrics.service.BMICalculator;
import com.ironcore.domain.bodymetrics.service.FatMassCalculator;
import com.ironcore.domain.bodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.bodymetrics.service.NavyBodyFatCalculator;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithFemaleRequiredCircumferences;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithInsufficientFemaleCircumferences;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithInsufficientMaleCircumferences;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithMaleRequiredCircumferences;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithoutCircumferences;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithoutHeight;
import static com.ironcore.application.bodymetrics.UpdateBodyMetricsUseCaseTestFactory.commandWithoutWeight;
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
class UpdateBodyMetricsUseCaseTest {

    private static final LocalDateTime EXISTING_MEASURED_AT = LocalDateTime.of(2026, 5, 1, 8, 0);
    private static final LocalDateTime EXISTING_UPDATED_AT = LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDateTime UPDATED_METRICS_AT = LocalDateTime.of(2026, 5, 23, 10, 0);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private BodyMetricsRepository bodyMetricsRepository;

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

    private UpdateBodyMetricsUseCase updateBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        bodyFatPercentageCalculator = new BodyFatPercentageCalculator(navyBodyFatCalculator);
        updateBodyMetricsUseCase = new UpdateBodyMetricsUseCase(
                userRepository,
                personRepository,
                bodyMetricsRepository,
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
            UpdateBodyMetricsCommand command = commandWithoutCircumferences();
            double expectedBmi = command.weight().value() / Math.pow(command.height().inMeters(), 2);

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            UpdateBodyMetricsResult result = updateBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);
            ArgumentCaptor<LoggableData> auditBeforeStateCaptor = ArgumentCaptor.forClass(LoggableData.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.bodyMetricsId().value()),
                    auditBeforeStateCaptor.capture(),
                    auditAfterStateCaptor.capture()
            );

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();
            BodyMetricsAuditData auditBeforeState = (BodyMetricsAuditData) auditBeforeStateCaptor.getValue();
            BodyMetricsAuditData auditAfterState = (BodyMetricsAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedBodyMetrics.getId()).isEqualTo(command.bodyMetricsId());
            assertThat(savedBodyMetrics.getUserId()).isEqualTo(command.userId());
            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedBodyMetrics.getWeight()).isEqualTo(command.weight());
            assertThat(savedBodyMetrics.getHeight()).isEqualTo(command.height());
            assertThat(savedBodyMetrics.getCircumferences()).isNull();
            assertThat(savedBodyMetrics.getBmi().value()).isCloseTo(expectedBmi, within(0.0001));
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            assertThat(savedBodyMetrics.getNotes()).isEqualTo(command.notes());

            assertThat(result.id()).isEqualTo(command.bodyMetricsId());
            assertThat(result.userId()).isEqualTo(command.userId());
            assertThat(result.measuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(result.updatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(result.weight()).isEqualTo(command.weight());
            assertThat(result.height()).isEqualTo(command.height());
            assertThat(result.bmi()).isEqualTo(savedBodyMetrics.getBmi());

            assertThat(auditBeforeState.id()).isEqualTo(command.bodyMetricsId().value());
            assertThat(auditBeforeState.userId()).isEqualTo(command.userId().value());
            assertThat(auditBeforeState.measuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(auditBeforeState.weightKg()).isEqualTo(80.0);
            assertThat(auditBeforeState.heightCm()).isEqualTo(180.0);
            assertThat(auditBeforeState.bmi()).isEqualTo(24.69);
            assertThat(auditBeforeState.notes()).isEqualTo("OLD TEXT");

            assertThat(auditAfterState.id()).isEqualTo(command.bodyMetricsId().value());
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
            UpdateBodyMetricsCommand command = commandWithMaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.MALE, command.height(), command.circumferences());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.bodyMetricsId().value()),
                    any(BodyMetricsAuditData.class),
                    any(BodyMetricsAuditData.class)
            );

            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldCalculateBodyFatFatMassAndLeanMassForFemaleUserWithSufficientCircumferences() {
            User user = activeFemaleUser();
            UpdateBodyMetricsCommand command = commandWithFemaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.FEMALE, command.height(), command.circumferences());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.bodyMetricsId().value()),
                    any(BodyMetricsAuditData.class),
                    any(BodyMetricsAuditData.class)
            );

            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesWhenCircumferencesAreInsufficient() {
            User user = activeUser();
            UpdateBodyMetricsCommand command = commandWithInsufficientMaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.bodyMetricsId().value()),
                    any(BodyMetricsAuditData.class),
                    any(BodyMetricsAuditData.class)
            );
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesForFemaleUserWhenHipIsMissing() {
            User user = activeFemaleUser();
            UpdateBodyMetricsCommand command = commandWithInsufficientFemaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenExistingUserBodyMetrics(command);
            givenUpdatedUserBodyMetricsIsPersisted();

            updateBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(EXISTING_MEASURED_AT);
            assertThat(savedBodyMetrics.getUpdatedAt()).isEqualTo(UPDATED_METRICS_AT);
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.UPDATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(command.bodyMetricsId().value()),
                    any(BodyMetricsAuditData.class),
                    any(BodyMetricsAuditData.class)
            );
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdateBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updateBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UpdateBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> updateBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).save(any());
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
            UpdateBodyMetricsCommand command = commandWithoutWeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> updateBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldFailWhenHeightIsMissing() {
            User user = activeUser();
            UpdateBodyMetricsCommand command = commandWithoutHeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> updateBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository, never()).findByIdAndUserId(any(), any());
            verify(bodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(auditLogPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    class BodyMetricsValidation {

        @Test
        void shouldFailWhenUserBodyMetricsDoesNotExistForUser() {
            User user = activeUser();
            UpdateBodyMetricsCommand command = commandWithoutCircumferences();

            givenFixedClock();
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            when(bodyMetricsRepository.findByIdAndUserId(command.bodyMetricsId(), command.userId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> updateBodyMetricsUseCase.execute(command))
                    .withMessage("Métricas corporais não encontradas.");

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(bodyMetricsRepository).findByIdAndUserId(command.bodyMetricsId(), command.userId());
            verify(bodyMetricsRepository, never()).save(any());
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

    private BodyMetrics givenExistingUserBodyMetrics(UpdateBodyMetricsCommand command) {
        BodyMetrics existingBodyMetrics = BodyMetrics.restore(
                command.bodyMetricsId(),
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

        when(bodyMetricsRepository.findByIdAndUserId(command.bodyMetricsId(), command.userId()))
                .thenReturn(Optional.of(existingBodyMetrics));

        return existingBodyMetrics;
    }

    private void givenUpdatedUserBodyMetricsIsPersisted() {
        when(bodyMetricsRepository.save(any(BodyMetrics.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenPersonExists(User user, SexType sexType) {
        Person person = Person.restore(
                user.getPersonId(),
                "Renan",
                new Sex(sexType),
                new BirthDate(LocalDate.of(1994, 4, 9)),
                CREATED_AT,
                UPDATED_AT
        );

        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
    }

    private User activeFemaleUser() {
        return User.restore(
                new UserId(1L),
                "Renata",
                personId(1L),
                email("renata@example.com"),
                passwordHash("hashed-password"),
                false,
                true,
                CREATED_AT,
                UPDATED_AT
        );
    }
}
