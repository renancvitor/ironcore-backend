package com.ironcore.application.workoutplanning.workoutday.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.delete.DeleteWorkoutDayUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
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
import java.util.List;
import java.util.Optional;

import static com.ironcore.application.workoutplanning.workoutday.WorkoutDayUseCaseTestFactory.validDeleteCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteWorkoutDayUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private Clock clock;

    private DeleteWorkoutDayUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteWorkoutDayUseCase(
                userRepository,
                personRepository,
                workoutDayRepository,
                workoutCycleRepository,
                clock
        );
    }

    @Nested
    class SuccessfulDelete {

        @Test
        void shouldDeleteWorkoutDayAndNormalizeRemainingOrder() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay deletedDay = restoredWorkoutDay();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            DeleteWorkoutDayCommand command = validDeleteCommand();
            WorkoutDay remainingDay = WorkoutDay.restore(
                    new WorkoutDayId(2L),
                    cycle.getId(),
                    WeekDay.WEDNESDAY,
                    "Treino seguinte",
                    3,
                    deletedDay.getCreatedAt(),
                    null
            );
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutDay(user, person, deletedDay, cycle, command);
            when(workoutDayRepository.findByWorkoutCycleId(cycle.getId())).thenReturn(List.of(remainingDay));
            when(workoutDayRepository.save(any(WorkoutDay.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            useCase.execute(command);

            ArgumentCaptor<WorkoutDay> captor = ArgumentCaptor.forClass(WorkoutDay.class);
            verify(workoutDayRepository).deleteById(deletedDay.getId());
            verify(workoutDayRepository).save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(remainingDay.getId());
            assertThat(captor.getValue().getSortOrder()).isEqualTo(1);
            assertThat(captor.getValue().getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            DeleteWorkoutDayCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutDayRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            DeleteWorkoutDayCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutDayRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            DeleteWorkoutDayCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutDayRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenWorkoutDayDoesNotBelongToPerson() {
            User user = activeUser();
            DeleteWorkoutDayCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), user.getPersonId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Dia de treino não encontrado.");
            verify(workoutDayRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            DeleteWorkoutDayCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.of(workoutDay));
            when(workoutCycleRepository.findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");
            verify(workoutDayRepository, never()).deleteById(any());
        }
    }

    @Nested
    class WorkoutCycleStatusValidation {

        @Test
        void shouldFailWhenWorkoutCycleIsCompleted() {
            assertBlockedCycle(completedWorkoutCycle());
        }

        @Test
        void shouldFailWhenWorkoutCycleIsCancelled() {
            assertBlockedCycle(cancelledWorkoutCycle());
        }
    }

    private void assertBlockedCycle(WorkoutCycle cycle) {
        User user = activeUser();
        Person person = restoredPerson();
        WorkoutDay workoutDay = restoredWorkoutDay();
        DeleteWorkoutDayCommand command = validDeleteCommand();
        givenEditableWorkoutDay(user, person, workoutDay, cycle, command);

        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Não é permitido excluir dias de treino de ciclos concluídos ou cancelados.");
        verify(workoutDayRepository, never()).deleteById(any());
    }

    private void givenEditableWorkoutDay(
            User user,
            Person person,
            WorkoutDay workoutDay,
            WorkoutCycle cycle,
            DeleteWorkoutDayCommand command
    ) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutDayRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(workoutDay));
        when(workoutCycleRepository.findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId()))
                .thenReturn(Optional.of(cycle));
    }

    private void givenFixedClock(LocalDateTime dateTime) {
        Clock fixedClock = Clock.fixed(
                Instant.from(dateTime.toInstant(ZoneOffset.UTC)),
                ZoneOffset.UTC
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }
}
