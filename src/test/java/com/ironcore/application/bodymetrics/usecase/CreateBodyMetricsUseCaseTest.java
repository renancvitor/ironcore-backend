package com.ironcore.application.bodymetrics.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.bodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsCommand;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsResult;
import com.ironcore.application.bodymetrics.create.CreateBodyMetricsUseCase;
import com.ironcore.application.bodymetrics.BodyMetricsAuditData;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.person.enums.SexType;
import com.ironcore.domain.person.model.Person;
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
import com.ironcore.domain.bodymetrics.valueobject.BodyMetricsId;
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

import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithFemaleRequiredCircumferences;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithInsufficientFemaleCircumferences;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithInsufficientMaleCircumferences;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithMaleRequiredCircumferences;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithoutCircumferences;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithoutHeight;
import static com.ironcore.application.bodymetrics.CreateBodyMetricsUseCaseTestFactory.commandWithoutWeight;
import static com.ironcore.domain.user.UserTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBodyMetricsUseCaseTest {

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

    private CreateBodyMetricsUseCase createBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        bodyFatPercentageCalculator = new BodyFatPercentageCalculator(navyBodyFatCalculator);
        createBodyMetricsUseCase = new CreateBodyMetricsUseCase(
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
    class SuccessfulCreation {

        @Test
        void shouldCreateUserBodyMetricsWithWeightAndHeight() {
            User user = activeUser();
            CreateBodyMetricsCommand command = commandWithoutCircumferences();
            LocalDateTime measuredAt = LocalDateTime.of(2026, 5, 23, 10, 0);
            double expectedBmi = command.weight().value() / Math.pow(command.height().inMeters(), 2);

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            CreateBodyMetricsResult result = createBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    auditAfterStateCaptor.capture()
            );

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();
            BodyMetricsAuditData auditAfterState = (BodyMetricsAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedBodyMetrics.getUserId()).isEqualTo(command.userId());
            assertThat(savedBodyMetrics.getMeasuredAt()).isEqualTo(measuredAt);
            assertThat(savedBodyMetrics.getWeight()).isEqualTo(command.weight());
            assertThat(savedBodyMetrics.getHeight()).isEqualTo(command.height());
            assertThat(savedBodyMetrics.getCircumferences()).isNull();
            assertThat(savedBodyMetrics.getBmi().value()).isCloseTo(expectedBmi, within(0.0001));
            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            assertThat(savedBodyMetrics.getNotes()).isEqualTo(command.notes());

            assertThat(result.id()).isEqualTo(new BodyMetricsId(1L));
            assertThat(result.userId()).isEqualTo(command.userId());
            assertThat(result.measuredAt()).isEqualTo(measuredAt);
            assertThat(result.bmi()).isEqualTo(savedBodyMetrics.getBmi());

            assertThat(auditAfterState.id()).isEqualTo(1L);
            assertThat(auditAfterState.userId()).isEqualTo(command.userId().value());
            assertThat(auditAfterState.measuredAt()).isEqualTo(measuredAt);
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
            CreateBodyMetricsCommand command = commandWithMaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            createBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.MALE, command.height(), command.circumferences());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(BodyMetricsAuditData.class)
            );

            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldCalculateBodyFatFatMassAndLeanMassForFemaleUserWithSufficientCircumferences() {
            User user = activeFemaleUser();
            CreateBodyMetricsCommand command = commandWithFemaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenUserBodyMetricsIsPersisted();

            createBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.FEMALE, command.height(), command.circumferences());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(BodyMetricsAuditData.class)
            );

            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesWhenCircumferencesAreInsufficient() {
            User user = activeUser();
            CreateBodyMetricsCommand command = commandWithInsufficientMaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            createBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(BodyMetricsAuditData.class)
            );
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesForFemaleUserWhenHipIsMissing() {
            User user = activeFemaleUser();
            CreateBodyMetricsCommand command = commandWithInsufficientFemaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenUserBodyMetricsIsPersisted();

            createBodyMetricsUseCase.execute(command);

            ArgumentCaptor<BodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(BodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(bodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            BodyMetrics savedBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(BodyMetricsAuditData.class)
            );
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CreateBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> createBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
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
            CreateBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> createBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
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
            CreateBodyMetricsCommand command = commandWithoutWeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> createBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
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
            CreateBodyMetricsCommand command = commandWithoutHeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> createBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(bodyMetricsRepository, never()).save(any());
            verify(bmiCalculator, never()).calculate(any(), any());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
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

    private void givenUserBodyMetricsIsPersisted() {
        when(bodyMetricsRepository.save(any(BodyMetrics.class)))
                .thenAnswer(invocation -> {
                    BodyMetrics bodyMetrics = invocation.getArgument(0);

                    return BodyMetrics.restore(
                            new BodyMetricsId(1L),
                            bodyMetrics.getUserId(),
                            bodyMetrics.getMeasuredAt(),
                            bodyMetrics.getWeight(),
                            bodyMetrics.getHeight(),
                            bodyMetrics.getCircumferences(),
                            bodyMetrics.getBmi(),
                            bodyMetrics.getBodyFatPercentage(),
                            bodyMetrics.getFatMassKg(),
                            bodyMetrics.getLeanMassKg(),
                            bodyMetrics.getUpdatedAt(),
                            bodyMetrics.getNotes()
                    );
                });
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
