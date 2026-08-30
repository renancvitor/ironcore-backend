package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutcycle.WorkoutCycleAuditData;
import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.delete.DeleteWorkoutCycleUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.workoutplanning.workoutcycle.DeleteWorkoutCycleUseCaseTestFactory.validCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.restoredWorkoutCycle;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteWorkoutCycleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private AuditLogPublisher publisher;

    private DeleteWorkoutCycleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteWorkoutCycleUseCase(userRepository, personRepository, workoutCycleRepository, publisher);
    }

    @Nested
    class SuccessfulDeletion {

        @Test
        void shouldDeleteWorkoutCycleWhenNotStarted() {
            DeleteWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle workoutCycle = restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null);
            givenOwnedWorkoutCycle(user, person, workoutCycle, command);

            useCase.execute(command);

            verify(workoutCycleRepository).deleteById(workoutCycle.getId());
            verify(publisher).publish(
                    eq(AuditActionType.DELETE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_CYCLE), eq(workoutCycle.getId().value()),
                    any(WorkoutCycleAuditData.class), isNull()
            );
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            DeleteWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(workoutCycleRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            DeleteWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(workoutCycleRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            DeleteWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");

            verify(workoutCycleRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            DeleteWorkoutCycleCommand command = validCommand();
            User user = activeUser();
            Person person = restoredPerson();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");

            verify(workoutCycleRepository, never()).deleteById(any());
        }
    }

    @Nested
    class BusinessValidation {

        @Test
        void shouldFailWhenWorkoutCycleIsInProgress() {
            assertCannotDeleteWorkoutCycle(inProgressWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            assertCannotDeleteWorkoutCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertCannotDeleteWorkoutCycle(cancelledWorkoutCycle());
        }
    }

    private void assertCannotDeleteWorkoutCycle(WorkoutCycle workoutCycle) {
        DeleteWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenOwnedWorkoutCycle(user, person, workoutCycle, command);

        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Não é permitido excluir ciclos de treino iniciados, concluídos ou cancelados.");

        verify(workoutCycleRepository, never()).deleteById(any());
    }

    private void givenOwnedWorkoutCycle(
            User user,
            Person person,
            WorkoutCycle workoutCycle,
            DeleteWorkoutCycleCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutCycle));
    }
}
