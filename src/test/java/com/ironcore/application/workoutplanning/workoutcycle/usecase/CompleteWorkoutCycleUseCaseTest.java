package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutcycle.WorkoutCycleAuditData;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.complete.CompleteWorkoutCycleUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.workoutplanning.workoutcycle.CompleteWorkoutCycleUseCaseTestFactory.validCommand;
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
class CompleteWorkoutCycleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private CompleteWorkoutCycleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteWorkoutCycleUseCase(
                userRepository,
                personRepository,
                workoutCycleRepository,
                clock,
                publisher
        );
    }

    @Nested
    class SuccessfulCompletion {

        @Test
        void shouldCompleteWorkoutCycleWithCurrentDate() {
            CompleteWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = inProgressWorkoutCycle();
            LocalDate endDate = LocalDate.of(2026, 8, 25);

            givenFixedClock(endDate);
            givenOwnedWorkoutCycle(user, person, workoutCycle, command);
            when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CompleteWorkoutCycleResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutCycle> captor = ArgumentCaptor.forClass(WorkoutCycle.class);
            verify(workoutCycleRepository).save(captor.capture());

            WorkoutCycle savedWorkoutCycle = captor.getValue();
            assertThat(savedWorkoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.COMPLETED);
            assertThat(savedWorkoutCycle.getEndDate()).isEqualTo(endDate);
            assertThat(result.id()).isEqualTo(workoutCycle.getId());
            assertThat(result.trainingGoalId()).isEqualTo(workoutCycle.getTrainingGoalId());
            assertThat(result.startDate()).isEqualTo(workoutCycle.getStartDate());
            assertThat(result.endDate()).isEqualTo(endDate);
            assertThat(result.workoutStatus()).isEqualTo(WorkoutStatus.COMPLETED);
            verify(publisher).publish(
                    eq(AuditActionType.UPDATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_CYCLE), eq(workoutCycle.getId().value()),
                    any(WorkoutCycleAuditData.class), any(WorkoutCycleAuditData.class)
            );
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CompleteWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            CompleteWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            CompleteWorkoutCycleCommand command = validCommand();
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
            CompleteWorkoutCycleCommand command = validCommand();
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
        void shouldFailWhenWorkoutCycleIsNotStarted() {
            assertCannotCompleteWorkoutCycle(restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null));
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            assertCannotCompleteWorkoutCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertCannotCompleteWorkoutCycle(cancelledWorkoutCycle());
        }
    }

    private void assertCannotCompleteWorkoutCycle(WorkoutCycle workoutCycle) {
        CompleteWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenFixedClock(LocalDate.of(2026, 8, 25));
        givenOwnedWorkoutCycle(user, person, workoutCycle, command);

        assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Somente um ciclo em andamento pode ser concluído.");

        verify(workoutCycleRepository, never()).save(any());
    }

    private void givenOwnedWorkoutCycle(
            User user,
            Person person,
            WorkoutCycle workoutCycle,
            CompleteWorkoutCycleCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutCycle));
    }

    private void givenFixedClock(LocalDate date) {
        Clock fixed = Clock.fixed(Instant.from(date.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
    }
}
