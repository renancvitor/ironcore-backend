package com.ironcore.application.workoutplanning.workoutactivity.usecase;

import static com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityUseCaseTestFactory.validReorderCommand;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.cancelledWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.completedWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.reorder.ReorderWorkoutActivityUseCase;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReorderWorkoutActivityUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutActivityRepository workoutActivityRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private Clock clock;

    private ReorderWorkoutActivityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new ReorderWorkoutActivityUseCase(
                        userRepository,
                        personRepository,
                        workoutActivityRepository,
                        workoutDayRepository,
                        workoutCycleRepository,
                        clock);
    }

    @Nested
    class SuccessfulReorder {
        @Test
        void shouldReorderWorkoutActivitiesWithinWorkoutDay() {
            ReorderWorkoutActivityCommand command = validReorderCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutActivity moved = activity(new WorkoutActivityId(1L), 1);
            WorkoutActivity second = activity(new WorkoutActivityId(2L), 2);
            WorkoutDay day = restoredWorkoutDay();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 23, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutActivity(user, person, moved, day, cycle, command);
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), day.getId()))
                    .thenReturn(List.of(moved, second));
            when(workoutActivityRepository.save(any(WorkoutActivity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            useCase.execute(command);

            ArgumentCaptor<WorkoutActivity> captor = ArgumentCaptor.forClass(WorkoutActivity.class);
            verify(workoutActivityRepository, times(2)).save(captor.capture());
            assertThat(captor.getAllValues())
                    .extracting(WorkoutActivity::getId)
                    .containsExactly(second.getId(), moved.getId());
            assertThat(captor.getAllValues())
                    .extracting(WorkoutActivity::getOrderIndex)
                    .containsExactly(1, 2);
            assertThat(captor.getAllValues())
                    .allSatisfy(activity -> assertThat(activity.getUpdatedAt()).isEqualTo(updatedAt));
        }
    }

    @Nested
    class AccessValidation {
        @Test
        void shouldFailWhenUserDoesNotExist() {
            ReorderWorkoutActivityCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            ReorderWorkoutActivityCommand command = validReorderCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            ReorderWorkoutActivityCommand command = validReorderCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutActivityDoesNotBelongToPerson() {
            ReorderWorkoutActivityCommand command = validReorderCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutActivityRepository.findByIdAndPersonId(command.id(), user.getPersonId()))
                    .thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Atividade de treino não encontrada.");
            verify(workoutActivityRepository, never()).save(any());
        }
    }

    @Nested
    class ReorderValidation {
        @Test
        void shouldFailWhenRequestedPositionIsNotPositive() {
            ReorderWorkoutActivityCommand command =
                    new ReorderWorkoutActivityCommand(
                            validReorderCommand().actorUserId(), new WorkoutActivityId(1L), 0);
            givenEditableWorkoutActivity(
                    activeUser(),
                    restoredPerson(),
                    restoredWorkoutActivity(),
                    restoredWorkoutDay(),
                    inProgressWorkoutCycle(),
                    command);
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Posição de ordenação inválida.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenRequestedPositionIsOutsideWorkoutDayRange() {
            ReorderWorkoutActivityCommand command =
                    new ReorderWorkoutActivityCommand(
                            validReorderCommand().actorUserId(), new WorkoutActivityId(1L), 3);
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity activity = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutActivity(user, person, activity, day, inProgressWorkoutCycle(), command);
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), day.getId()))
                    .thenReturn(List.of(activity));
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Posição de ordenação inválida.");
            verify(workoutActivityRepository, never()).save(any());
        }

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
        ReorderWorkoutActivityCommand command = validReorderCommand();
        givenEditableWorkoutActivity(
                activeUser(),
                restoredPerson(),
                restoredWorkoutActivity(),
                restoredWorkoutDay(),
                cycle,
                command);
        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage(
                        "Não é permitido editar atividades de treino de ciclos concluídos ou cancelados.");
        verify(workoutActivityRepository, never()).save(any());
    }

    private void givenEditableWorkoutActivity(
            User user,
            Person person,
            WorkoutActivity activity,
            WorkoutDay day,
            WorkoutCycle cycle,
            ReorderWorkoutActivityCommand command) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutActivityRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(activity));
        when(workoutDayRepository.findByIdAndPersonId(activity.getWorkoutDayId(), person.getId()))
                .thenReturn(Optional.of(day));
        when(workoutCycleRepository.findByIdAndPersonId(day.getWorkoutCycleId(), person.getId()))
                .thenReturn(Optional.of(cycle));
    }

    private WorkoutActivity activity(WorkoutActivityId id, int orderIndex) {
        return WorkoutActivity.restore(
                id,
                restoredWorkoutDay().getId(),
                new ExerciseId(id.value()),
                orderIndex,
                4,
                8,
                12,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null);
    }

    private void givenFixedClock(LocalDateTime dateTime) {
        Clock fixed = Clock.fixed(Instant.from(dateTime.toInstant(ZoneOffset.UTC)), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
    }
}
