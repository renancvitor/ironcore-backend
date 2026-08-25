package com.ironcore.application.workoutplanning.workoutcycle.usecase;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleCommand;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleResult;
import com.ironcore.application.workoutplanning.workoutcycle.cancel.CancelWorkoutCycleUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.exception.InvalidWorkoutCycleException;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ironcore.application.workoutplanning.workoutcycle.CancelWorkoutCycleUseCaseTestFactory.validCommand;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelWorkoutCycleUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    private CancelWorkoutCycleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelWorkoutCycleUseCase(userRepository, personRepository, workoutCycleRepository);
    }

    @Nested
    class SuccessfulCancellation {

        @Test
        void shouldCancelWorkoutCycleWhenNotStarted() {
            assertWorkoutCycleCancelled(restoredWorkoutCycle(WorkoutStatus.NOT_STARTED, null, null));
        }

        @Test
        void shouldCancelWorkoutCycleWhenInProgress() {
            assertWorkoutCycleCancelled(inProgressWorkoutCycle());
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CancelWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            CancelWorkoutCycleCommand command = validCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");

            verify(workoutCycleRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            CancelWorkoutCycleCommand command = validCommand();
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
            CancelWorkoutCycleCommand command = validCommand();
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
            assertCannotCancelWorkoutCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertCannotCancelWorkoutCycle(cancelledWorkoutCycle());
        }
    }

    private void assertWorkoutCycleCancelled(WorkoutCycle workoutCycle) {
        CancelWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenOwnedWorkoutCycle(user, person, workoutCycle, command);
        when(workoutCycleRepository.save(any(WorkoutCycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CancelWorkoutCycleResult result = useCase.execute(command);

        ArgumentCaptor<WorkoutCycle> captor = ArgumentCaptor.forClass(WorkoutCycle.class);
        verify(workoutCycleRepository).save(captor.capture());

        WorkoutCycle savedWorkoutCycle = captor.getValue();
        assertThat(savedWorkoutCycle.getWorkoutStatus()).isEqualTo(WorkoutStatus.CANCELLED);
        assertThat(result.id()).isEqualTo(workoutCycle.getId());
        assertThat(result.trainingGoalId()).isEqualTo(workoutCycle.getTrainingGoalId());
        assertThat(result.workoutStatus()).isEqualTo(WorkoutStatus.CANCELLED);
    }

    private void assertCannotCancelWorkoutCycle(WorkoutCycle workoutCycle) {
        CancelWorkoutCycleCommand command = validCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenOwnedWorkoutCycle(user, person, workoutCycle, command);

        assertThatExceptionOfType(InvalidWorkoutCycleException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Um ciclo concluído ou cancelado não pode ser cancelado.");

        verify(workoutCycleRepository, never()).save(any());
    }

    private void givenOwnedWorkoutCycle(
            User user,
            Person person,
            WorkoutCycle workoutCycle,
            CancelWorkoutCycleCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutCycleRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutCycle));
    }
}
