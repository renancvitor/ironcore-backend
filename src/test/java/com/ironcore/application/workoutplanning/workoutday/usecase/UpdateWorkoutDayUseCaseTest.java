package com.ironcore.application.workoutplanning.workoutday.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayResult;
import com.ironcore.application.workoutplanning.workoutday.update.UpdateWorkoutDayUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
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

import static com.ironcore.application.workoutplanning.workoutday.WorkoutDayUseCaseTestFactory.validUpdateCommand;
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
class UpdateWorkoutDayUseCaseTest {

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

    private UpdateWorkoutDayUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateWorkoutDayUseCase(
                userRepository,
                personRepository,
                workoutDayRepository,
                workoutCycleRepository,
                clock
        );
    }

    @Nested
    class SuccessfulUpdate {

        @Test
        void shouldUpdateWorkoutDayTitle() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            UpdateWorkoutDayCommand command = validUpdateCommand();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 22, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutDay(user, person, workoutDay, cycle, command);
            when(workoutDayRepository.save(any(WorkoutDay.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateWorkoutDayResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutDay> captor = ArgumentCaptor.forClass(WorkoutDay.class);
            verify(workoutDayRepository).save(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo(command.title());
            assertThat(captor.getValue().getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(result.title()).isEqualTo(command.title());
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdateWorkoutDayCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutDayRepository, never()).findByIdAndPersonId(any(), any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            UpdateWorkoutDayCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(personRepository, never()).findById(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            UpdateWorkoutDayCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutDayRepository, never()).findByIdAndPersonId(any(), any());
        }

        @Test
        void shouldFailWhenWorkoutDayDoesNotBelongToPerson() {
            User user = activeUser();
            UpdateWorkoutDayCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), user.getPersonId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Dia de treino não encontrado.");
            verify(workoutCycleRepository, never()).findByIdAndPersonId(any(), any());
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            UpdateWorkoutDayCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutDayRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.of(workoutDay));
            when(workoutCycleRepository.findByIdAndPersonId(workoutDay.getWorkoutCycleId(), person.getId()))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Ciclo de treino não encontrado.");
            verify(workoutDayRepository, never()).save(any());
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
        UpdateWorkoutDayCommand command = validUpdateCommand();
        givenEditableWorkoutDay(user, person, workoutDay, cycle, command);

        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Não é permitido editar dias de treino de ciclos concluídos ou cancelados.");
        verify(workoutDayRepository, never()).save(any());
    }

    private void givenEditableWorkoutDay(
            User user,
            Person person,
            WorkoutDay workoutDay,
            WorkoutCycle cycle,
            UpdateWorkoutDayCommand command
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
