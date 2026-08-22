package com.ironcore.application.workoutplanning.workoutday.usecase;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayCommand;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayResult;
import com.ironcore.application.workoutplanning.workoutday.create.CreateWorkoutDayUseCase;
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

import static com.ironcore.application.workoutplanning.workoutday.WorkoutDayUseCaseTestFactory.validCreateCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWorkoutDayUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private Clock clock;

    private CreateWorkoutDayUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWorkoutDayUseCase(
                userRepository,
                personRepository,
                workoutCycleRepository,
                workoutDayRepository,
                clock
        );
    }

    @Nested
    class SuccessfulCreation {

        @Test
        void shouldCreateWorkoutDayAtNextPositionForWeekDay() {
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            CreateWorkoutDayCommand command = validCreateCommand();
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 12, 0);
            WorkoutDay existingDay = WorkoutDay.restore(
                    new WorkoutDayId(2L),
                    cycle.getId(),
                    WeekDay.MONDAY,
                    "Treino existente",
                    2,
                    createdAt.minusDays(1),
                    null
            );
            givenFixedClock(createdAt);
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
            when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId()))
                    .thenReturn(Optional.of(cycle));
            when(workoutDayRepository.findByWorkoutCycleId(cycle.getId())).thenReturn(List.of(existingDay));
            when(workoutDayRepository.save(any(WorkoutDay.class)))
                    .thenAnswer(invocation -> WorkoutDay.restore(
                            new WorkoutDayId(3L),
                            cycle.getId(),
                            WeekDay.MONDAY,
                            command.title(),
                            3,
                            createdAt,
                            null
                    ));

            CreateWorkoutDayResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutDay> captor = ArgumentCaptor.forClass(WorkoutDay.class);
            verify(workoutDayRepository).save(captor.capture());
            assertThat(captor.getValue().getSortOrder()).isEqualTo(3);
            assertThat(captor.getValue().getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.id()).isEqualTo(new WorkoutDayId(3L));
            assertThat(result.sortOrder()).isEqualTo(3);
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldFailWhenUserDoesNotExist() {
            CreateWorkoutDayCommand command = validCreateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            User user = inactiveUser();
            CreateWorkoutDayCommand command = validCreateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));

            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(personRepository, never()).findById(any());
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            User user = activeUser();
            CreateWorkoutDayCommand command = validCreateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());

            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutCycleRepository, never()).findByIdAndPersonId(any(), any());
            verify(workoutDayRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutCycleDoesNotBelongToPerson() {
            User user = activeUser();
            CreateWorkoutDayCommand command = validCreateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), user.getPersonId()))
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
        CreateWorkoutDayCommand command = validCreateCommand();
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
        when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), user.getPersonId()))
                .thenReturn(Optional.of(cycle));

        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage("Não é permitido adicionar dias de treino de ciclos concluídos ou cancelados.");
        verify(workoutDayRepository, never()).save(any());
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
