package com.ironcore.application.userbodymetrics.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.userbodymetrics.component.BodyFatPercentageCalculator;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsCommand;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsResult;
import com.ironcore.application.userbodymetrics.create.CreateUserBodyMetricsUseCase;
import com.ironcore.application.userbodymetrics.UserBodyMetricsAuditData;
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
import com.ironcore.domain.userbodymetrics.model.UserBodyMetrics;
import com.ironcore.domain.userbodymetrics.repository.UserBodyMetricsRepository;
import com.ironcore.domain.userbodymetrics.service.BMICalculator;
import com.ironcore.domain.userbodymetrics.service.FatMassCalculator;
import com.ironcore.domain.userbodymetrics.service.LeanMassCalculator;
import com.ironcore.domain.userbodymetrics.service.NavyBodyFatCalculator;
import com.ironcore.domain.userbodymetrics.valueobject.UserBodyMetricsId;
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

import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithFemaleRequiredCircumferences;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithInsufficientFemaleCircumferences;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithInsufficientMaleCircumferences;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithMaleRequiredCircumferences;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithoutCircumferences;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithoutHeight;
import static com.ironcore.application.userbodymetrics.CreateUserBodyMetricsUseCaseTestFactory.commandWithoutWeight;
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
class CreateUserBodyMetricsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

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

    private CreateUserBodyMetricsUseCase createUserBodyMetricsUseCase;

    @BeforeEach
    void setUp() {
        bodyFatPercentageCalculator = new BodyFatPercentageCalculator(navyBodyFatCalculator);
        createUserBodyMetricsUseCase = new CreateUserBodyMetricsUseCase(
                userRepository,
                personRepository,
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
    class SuccessfulCreation {

        @Test
        void shouldCreateUserBodyMetricsWithWeightAndHeight() {
            User user = activeUser();
            CreateUserBodyMetricsCommand command = commandWithoutCircumferences();
            LocalDateTime measuredAt = LocalDateTime.of(2026, 5, 23, 10, 0);
            double expectedBmi = command.weight().value() / Math.pow(command.height().inMeters(), 2);

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            CreateUserBodyMetricsResult result = createUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);
            ArgumentCaptor<LoggableData> auditAfterStateCaptor = ArgumentCaptor.forClass(LoggableData.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    auditAfterStateCaptor.capture()
            );

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();
            UserBodyMetricsAuditData auditAfterState = (UserBodyMetricsAuditData) auditAfterStateCaptor.getValue();

            assertThat(savedUserBodyMetrics.getUserId()).isEqualTo(command.userId());
            assertThat(savedUserBodyMetrics.getMeasuredAt()).isEqualTo(measuredAt);
            assertThat(savedUserBodyMetrics.getWeight()).isEqualTo(command.weight());
            assertThat(savedUserBodyMetrics.getHeight()).isEqualTo(command.height());
            assertThat(savedUserBodyMetrics.getCircumferences()).isNull();
            assertThat(savedUserBodyMetrics.getBmi().value()).isCloseTo(expectedBmi, within(0.0001));
            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getNotes()).isEqualTo(command.notes());

            assertThat(result.id()).isEqualTo(new UserBodyMetricsId(1L));
            assertThat(result.userId()).isEqualTo(command.userId());
            assertThat(result.measuredAt()).isEqualTo(measuredAt);
            assertThat(result.bmi()).isEqualTo(savedUserBodyMetrics.getBmi());

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
            CreateUserBodyMetricsCommand command = commandWithMaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            createUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.MALE, command.height(), command.circumferences());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(UserBodyMetricsAuditData.class)
            );

            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldCalculateBodyFatFatMassAndLeanMassForFemaleUserWithSufficientCircumferences() {
            User user = activeFemaleUser();
            CreateUserBodyMetricsCommand command = commandWithFemaleRequiredCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenUserBodyMetricsIsPersisted();

            createUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator).calculate(SexType.FEMALE, command.height(), command.circumferences());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            verify(fatMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getBodyFatPercentage());
            verify(leanMassCalculator).calculate(command.weight(), savedUserBodyMetrics.getFatMassKg());
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(UserBodyMetricsAuditData.class)
            );

            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNotNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNotNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNotNull();
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesWhenCircumferencesAreInsufficient() {
            User user = activeUser();
            CreateUserBodyMetricsCommand command = commandWithInsufficientMaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);
            givenUserBodyMetricsIsPersisted();

            createUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(UserBodyMetricsAuditData.class)
            );
        }

        @Test
        void shouldNotCalculateBodyFatAndMassesForFemaleUserWhenHipIsMissing() {
            User user = activeFemaleUser();
            CreateUserBodyMetricsCommand command = commandWithInsufficientFemaleCircumferences();

            givenFixedClock();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.FEMALE);
            givenUserBodyMetricsIsPersisted();

            createUserBodyMetricsUseCase.execute(command);

            ArgumentCaptor<UserBodyMetrics> userBodyMetricsCaptor = ArgumentCaptor.forClass(UserBodyMetrics.class);

            verify(userRepository).findById(command.userId());
            verify(bmiCalculator).calculate(command.height(), command.weight());
            verify(navyBodyFatCalculator, never()).calculate(any(), any(), any());
            verify(fatMassCalculator, never()).calculate(any(), any());
            verify(leanMassCalculator, never()).calculate(any(), any());
            verify(userBodyMetricsRepository).save(userBodyMetricsCaptor.capture());

            UserBodyMetrics savedUserBodyMetrics = userBodyMetricsCaptor.getValue();

            assertThat(savedUserBodyMetrics.getBodyFatPercentage()).isNull();
            assertThat(savedUserBodyMetrics.getFatMassKg()).isNull();
            assertThat(savedUserBodyMetrics.getLeanMassKg()).isNull();
            verify(auditLogPublisher).publish(
                    eq(AuditActionType.CREATE),
                    eq(user.getId().value()),
                    eq(user.getEmail().value()),
                    eq(AuditTargetType.USER_BODY_METRICS),
                    eq(1L),
                    isNull(),
                    any(UserBodyMetricsAuditData.class)
            );
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CreateUserBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> createUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.userId());
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
            CreateUserBodyMetricsCommand command = commandWithoutCircumferences();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> createUserBodyMetricsUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.userId());
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
            CreateUserBodyMetricsCommand command = commandWithoutWeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> createUserBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
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
            CreateUserBodyMetricsCommand command = commandWithoutHeight();

            when(userRepository.findById(command.userId())).thenReturn(Optional.of(user));
            givenPersonExists(user, SexType.MALE);

            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> createUserBodyMetricsUseCase.execute(command))
                    .withMessage("Peso e altura são obrigatórios.");

            verify(userRepository).findById(command.userId());
            verify(userBodyMetricsRepository, never()).save(any());
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
        when(userBodyMetricsRepository.save(any(UserBodyMetrics.class)))
                .thenAnswer(invocation -> {
                    UserBodyMetrics userBodyMetrics = invocation.getArgument(0);

                    return UserBodyMetrics.restore(
                            new UserBodyMetricsId(1L),
                            userBodyMetrics.getUserId(),
                            userBodyMetrics.getMeasuredAt(),
                            userBodyMetrics.getWeight(),
                            userBodyMetrics.getHeight(),
                            userBodyMetrics.getCircumferences(),
                            userBodyMetrics.getBmi(),
                            userBodyMetrics.getBodyFatPercentage(),
                            userBodyMetrics.getFatMassKg(),
                            userBodyMetrics.getLeanMassKg(),
                            userBodyMetrics.getUpdatedAt(),
                            userBodyMetrics.getNotes()
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
