package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutcycle.WorkoutCycleAuditData;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.update.UpdateWorkoutCycleUseCase;
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
import com.ironcore.domain.workoutplanning.traininggoal.valueobject.TrainingGoalId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.workoutplanning.workoutcycle.UpdateWorkoutCycleUseCaseTestFactory.validCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.restoredWorkoutCycle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWorkoutCycleUseCaseTest {

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

    private UpdateWorkoutCycleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateWorkoutCycleUseCase(
                userRepository,
                personRepository,
                trainingGoalRepository,
                workoutCycleRepository,
                clock,
                publisher
        );
    }

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdateWorkoutCycleWhenNotStarted() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 24, 12, 0);

            givenFixedClock(updatedAt);
            givenEditableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(command.trainingGoalId()))
                    .thenReturn(Optional.of(activeTrainingGoal(command.trainingGoalId())));
            when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateWorkoutCycleResult result = useCase.execute(command);

            assertUpdatedWorkoutCycle(command, updatedAt, null, null, result);
            verify(publisher).publish(
                    eq(AuditActionType.UPDATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_CYCLE), eq(workoutCycle.getId().value()),
                    any(WorkoutCycleAuditData.class), any(WorkoutCycleAuditData.class)
            );
        }

        @Test
        void shouldUpdateWorkoutCycleWhenInProgressWithoutChangingDates() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 24, 12, 0);

            givenFixedClock(updatedAt);
            givenEditableWorkoutCycle(user, person, workoutCycle, command);
            when(trainingGoalRepository.findById(command.trainingGoalId()))
                    .thenReturn(Optional.of(activeTrainingGoal(command.trainingGoalId())));
            when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateWorkoutCycleResult result = useCase.execute(command);

            assertUpdatedWorkoutCycle(
                    command,
                    updatedAt,
                    workoutCycle.getStartDate(),
                    workoutCycle.getEndDate(),
                    result
            );
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdateWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            UpdateWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }
    }

    @Nested
    class BusinessValidation {

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            assertBlockedCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertBlockedCycle(cancelledWorkoutCycle());
        }

        @Test
        void shouldFailWhenTrainingGoalDoesNotExist() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            givenEditableWorkoutCycle(
                    user,
                    person,
                    restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null),
                    command
            );
            when(trainingGoalRepository.findById(command.trainingGoalId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Objetivo de treino não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenTrainingGoalIsInactive() {
            UpdateWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            givenEditableWorkoutCycle(
                    user,
                    person,
                    restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null),
                    command
            );
            when(trainingGoalRepository.findById(command.trainingGoalId()))
                    .thenReturn(Optional.of(inactiveTrainingGoal(command.trainingGoalId())));

            assertThatExceptionOfType(InvalidTrainingGoalException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Objetivo de treino inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }
    }

    private void assertUpdatedWorkoutCycle(
            UpdateWorkoutCycleCommand command,
            LocalDateTime updatedAt,
            LocalDate startDate,
            LocalDate endDate,
            UpdateWorkoutCycleResult result
    ) {
        ArgumentCaptor<WorkoutCycle> captor = ArgumentCaptor.forClass(WorkoutCycle.class);
        verify(workoutCycleRepository).save(captor.capture());

        WorkoutCycle savedWorkoutCycle = captor.getValue();
        assertThat(savedWorkoutCycle.getName()).isEqualTo(command.name());
        assertThat(savedWorkoutCycle.getTrainingGoalId()).isEqualTo(command.trainingGoalId());
        assertThat(savedWorkoutCycle.getDesiredDurationMonths()).isEqualTo(command.desiredDurationMonths());
        assertThat(savedWorkoutCycle.getNotes()).isEqualTo(command.notes());
        assertThat(savedWorkoutCycle.getStartDate()).isEqualTo(startDate);
        assertThat(savedWorkoutCycle.getEndDate()).isEqualTo(endDate);
        assertThat(savedWorkoutCycle.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.name()).isEqualTo(command.name());
        assertThat(result.trainingGoalId()).isEqualTo(command.trainingGoalId());
        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    private void assertBlockedCycle(WorkoutCycle workoutCycle) {
        UpdateWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenEditableWorkoutCycle(user, person, workoutCycle, command);

        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Não é permitido editar ciclos de treino concluídos ou cancelados.");

        verify(workoutCycleRepository, never()).save(any());
    }

    private void givenEditableWorkoutCycle(
            User user,
            Person person,
            WorkoutCycle workoutCycle,
            UpdateWorkoutCycleCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutCycle));
    }

    private void givenFixedClock(LocalDateTime dateTime) {
        Clock fixed = Clock.fixed(Instant.from(dateTime.toInstant(ZoneOffset.UTC)), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
    }

    private TrainingGoal activeTrainingGoal(TrainingGoalId id) {
        return TrainingGoal.restore(
                id,
                new TrainingGoalCode("strength"),
                "Força",
                true,
                10
        );
    }

    private TrainingGoal inactiveTrainingGoal(TrainingGoalId id) {
        return TrainingGoal.restore(
                id,
                new TrainingGoalCode("strength"),
                "Força",
                false,
                10
        );
    }
}
