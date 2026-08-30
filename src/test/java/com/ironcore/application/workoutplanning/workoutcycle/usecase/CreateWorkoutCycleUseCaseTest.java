package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutcycle.WorkoutCycleAuditData;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.create.CreateWorkoutCycleUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.traininggoal.exception.InvalidTrainingGoalException;
import com.ironcore.domain.workoutplanning.traininggoal.model.TrainingGoal;
import com.ironcore.domain.workoutplanning.traininggoal.repository.TrainingGoalRepository;
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalCode;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutOrigin;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.ironcore.application.workoutplanning.workoutcycle.CreateWorkoutCycleUseCaseTestFactory.validCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.traininggoal.TrainingGoalTestFactory.restoreTrainingGoal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWorkoutCycleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private TrainingGoalRepository trainingGoalRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private CreateWorkoutCycleUseCase createWorkoutCycleUseCase;

    @BeforeEach
    void setUp() {
        createWorkoutCycleUseCase = new CreateWorkoutCycleUseCase(
                userRepository,
                personRepository,
                trainingGoalRepository,
                workoutCycleRepository,
                clock,
                publisher
        );
    }

    @Nested
    class SuccessfulCreation {

        @Test
        void shouldCreateManualWorkoutCycleForAuthenticatedPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            TrainingGoal trainingGoal = restoreTrainingGoal();
            CreateWorkoutCycleCommand command = validCommand();
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 16, 12, 0);

            givenFixedClock();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(trainingGoalRepository.findById(command.trainingGoalId()))
                    .thenReturn(Optional.of(trainingGoal));
            givenWorkoutCycleIsPersisted();

            CreateWorkoutCycleResult result = createWorkoutCycleUseCase.execute(command);

            ArgumentCaptor<WorkoutCycle> workoutCycleCaptor = ArgumentCaptor.forClass(WorkoutCycle.class);

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(trainingGoalRepository).findById(command.trainingGoalId());
            verify(workoutCycleRepository).save(workoutCycleCaptor.capture());

            WorkoutCycle savedWorkoutCycle = workoutCycleCaptor.getValue();

            assertThat(savedWorkoutCycle.getId()).isNull();
            assertThat(savedWorkoutCycle.getPersonId()).isEqualTo(person.getId());
            assertThat(savedWorkoutCycle.getName()).isEqualTo(command.name());
            assertThat(savedWorkoutCycle.getTrainingGoalId()).isEqualTo(trainingGoal.getId());
            assertThat(savedWorkoutCycle.getDesiredDurationMonths()).isEqualTo(command.desiredDurationMonths());
            assertThat(savedWorkoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.NOT_STARTED);
            assertThat(savedWorkoutCycle.getWorkoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(savedWorkoutCycle.getNotes()).isEqualTo(command.notes());
            assertThat(savedWorkoutCycle.getStartDate()).isNull();
            assertThat(savedWorkoutCycle.getEndDate()).isNull();
            assertThat(savedWorkoutCycle.getCreatedAt()).isEqualTo(createdAt);
            assertThat(savedWorkoutCycle.getUpdatedAt()).isNull();

            assertThat(result.id()).isEqualTo(new WorkoutCycleId(1L));
            assertThat(result.personId()).isEqualTo(person.getId());
            assertThat(result.name()).isEqualTo(command.name());
            assertThat(result.trainingGoalId()).isEqualTo(trainingGoal.getId());
            assertThat(result.desiredDurationMonths()).isEqualTo(command.desiredDurationMonths());
            assertThat(result.workoutStatus()).isEqualTo(WorkoutStatus.NOT_STARTED);
            assertThat(result.workoutOrigin()).isEqualTo(WorkoutOrigin.MANUAL);
            assertThat(result.notes()).isEqualTo(command.notes());
            assertThat(result.createdAt()).isEqualTo(createdAt);
            verify(publisher).publish(
                    eq(AuditActionType.CREATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_CYCLE), eq(result.id().value()), isNull(),
                    any(WorkoutCycleAuditData.class)
            );
        }
    }

    @Nested
    class UserValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CreateWorkoutCycleCommand command = validCommand();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> createWorkoutCycleUseCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository, never()).findById(any());
            verify(trainingGoalRepository, never()).findById(any());
            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            CreateWorkoutCycleCommand command = validCommand();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> createWorkoutCycleUseCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository, never()).findById(any());
            verify(trainingGoalRepository, never()).findById(any());
            verify(workoutCycleRepository, never()).save(any());
        }
    }

    @Nested
    class PersonValidation {

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            CreateWorkoutCycleCommand command = validCommand();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> createWorkoutCycleUseCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(trainingGoalRepository, never()).findById(any());
            verify(workoutCycleRepository, never()).save(any());
        }
    }

    @Nested
    class TrainingGoalValidation {

        @Test
        void shouldFailWhenTrainingGoalDoesNotExist() {
            User user = activeUser();
            Person person = restoredPerson();
            CreateWorkoutCycleCommand command = validCommand();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(trainingGoalRepository.findById(command.trainingGoalId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> createWorkoutCycleUseCase.execute(command))
                    .withMessage("Objetivo de treino não encontrado.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(trainingGoalRepository).findById(command.trainingGoalId());
            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenTrainingGoalIsInactive() {
            User user = activeUser();
            Person person = restoredPerson();
            TrainingGoal trainingGoal = inactiveTrainingGoal();
            CreateWorkoutCycleCommand command = validCommand();

            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(trainingGoalRepository.findById(command.trainingGoalId()))
                    .thenReturn(Optional.of(trainingGoal));

            assertThatExceptionOfType(InvalidTrainingGoalException.class)
                    .isThrownBy(() -> createWorkoutCycleUseCase.execute(command))
                    .withMessage("Objetivo de treino inativo.");

            verify(userRepository).findById(command.actorUserId());
            verify(personRepository).findById(user.getPersonId());
            verify(trainingGoalRepository).findById(command.trainingGoalId());
            verify(workoutCycleRepository, never()).save(any());
        }
    }

    private void givenFixedClock() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-08-16T12:00:00Z"),
                ZoneOffset.UTC
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private void givenWorkoutCycleIsPersisted() {
        when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                .thenAnswer(invocation -> {
                    WorkoutCycle workoutCycle = invocation.getArgument(0);

                    return WorkoutCycle.restore(
                            new WorkoutCycleId(1L),
                            workoutCycle.getPersonId(),
                            workoutCycle.getName(),
                            workoutCycle.getTrainingGoalId(),
                            workoutCycle.getStartDate(),
                            workoutCycle.getEndDate(),
                            workoutCycle.getDesiredDurationMonths(),
                            workoutCycle.getWorkoutStatus(),
                            workoutCycle.getWorkoutOrigin(),
                            workoutCycle.getNotes(),
                            workoutCycle.getCreatedAt(),
                            workoutCycle.getUpdatedAt()
                    );
                });
    }

    private TrainingGoal inactiveTrainingGoal() {
        return TrainingGoal.restore(
                validCommand().trainingGoalId(),
                new TrainingGoalCode("hypertrophy"),
                "Hipertrofia",
                false,
                10
        );
    }
}
