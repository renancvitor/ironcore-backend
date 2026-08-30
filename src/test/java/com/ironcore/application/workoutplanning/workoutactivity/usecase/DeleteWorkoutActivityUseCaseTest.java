package com.ironcore.application.workoutplanning.workoutactivity.usecase;

import static com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityUseCaseTestFactory.validDeleteCommand;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ironcore.application.exception.OperationNotAllowedException;
import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityAuditData;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.delete.DeleteWorkoutActivityUseCase;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
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
import java.math.BigDecimal;
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
class DeleteWorkoutActivityUseCaseTest {

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

    @Mock
    private AuditLogPublisher publisher;

    private DeleteWorkoutActivityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new DeleteWorkoutActivityUseCase(
                        userRepository,
                        personRepository,
                        workoutActivityRepository,
                        workoutDayRepository,
                        workoutCycleRepository,
                        clock,
                        publisher
                );
    }

    @Nested
    class SuccessfulDeletion {
        @Test
        void shouldDeleteWorkoutActivityAndNormalizeRemainingOrder() {
            DeleteWorkoutActivityCommand command = validDeleteCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity deleted = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            WorkoutActivity remaining = activity(new WorkoutActivityId(2L), 2);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 23, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutActivity(user, person, deleted, day, cycle, command);
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), day.getId()))
                    .thenReturn(List.of(remaining));
            when(workoutActivityRepository.save(any(WorkoutActivity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            useCase.execute(command);

            verify(workoutActivityRepository).deleteById(deleted.getId());
            ArgumentCaptor<WorkoutActivity> captor = ArgumentCaptor.forClass(WorkoutActivity.class);
            verify(workoutActivityRepository).save(captor.capture());
            assertThat(captor.getValue().getOrderIndex()).isEqualTo(1);
            assertThat(captor.getValue().getUpdatedAt()).isEqualTo(updatedAt);
            verify(publisher).publish(
                    eq(AuditActionType.DELETE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_ACTIVITY), eq(deleted.getId().value()),
                    any(WorkoutActivityAuditData.class), isNull()
            );
        }
    }

    @Nested
    class AccessValidation {
        @Test
        void shouldFailWhenUserDoesNotExist() {
            DeleteWorkoutActivityCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutActivityRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            DeleteWorkoutActivityCommand command = validDeleteCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutActivityRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            DeleteWorkoutActivityCommand command = validDeleteCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutActivityRepository, never()).deleteById(any());
        }

        @Test
        void shouldFailWhenWorkoutActivityDoesNotBelongToPerson() {
            DeleteWorkoutActivityCommand command = validDeleteCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutActivityRepository.findByIdAndPersonId(command.id(), user.getPersonId()))
                    .thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Atividade de treino não encontrada.");
            verify(workoutActivityRepository, never()).deleteById(any());
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
        DeleteWorkoutActivityCommand command = validDeleteCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenEditableWorkoutActivity(
                user, person, restoredWorkoutActivity(), restoredWorkoutDay(), cycle, command);
        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage(
                        "Não é permitido excluir atividades de treino de ciclos concluídos ou cancelados.");
        verify(workoutActivityRepository, never()).deleteById(any());
    }

    private void givenEditableWorkoutActivity(
            User user,
            Person person,
            WorkoutActivity activity,
            WorkoutDay day,
            WorkoutCycle cycle,
            DeleteWorkoutActivityCommand command) {
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
                new com.ironcore.domain.exercise.valueobject.ExerciseId(2L),
                orderIndex,
                null,
                null,
                null,
                null,
                null,
                30,
                new BigDecimal("3.00"),
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
