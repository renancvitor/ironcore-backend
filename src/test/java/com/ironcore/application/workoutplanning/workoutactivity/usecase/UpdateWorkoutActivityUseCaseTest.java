package com.ironcore.application.workoutplanning.workoutactivity.usecase;

import static com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityUseCaseTestFactory.validUpdateCommand;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreInactiveExercise;
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
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityResult;
import com.ironcore.application.workoutplanning.workoutactivity.update.UpdateWorkoutActivityUseCase;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import java.time.Clock;
import java.time.Instant;
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

@ExtendWith(MockitoExtension.class)
class UpdateWorkoutActivityUseCaseTest {

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
    private ExerciseRepository exerciseRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private UpdateWorkoutActivityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new UpdateWorkoutActivityUseCase(
                        userRepository,
                        personRepository,
                        workoutActivityRepository,
                        workoutDayRepository,
                        workoutCycleRepository,
                        exerciseRepository,
                        clock,
                        publisher
                );
    }

    @Nested
    class SuccessfulUpdate {
        @Test
        void shouldUpdateWorkoutActivityAndExercise() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity activity = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 23, 12, 0);
            givenFixedClock(updatedAt);
            givenEditableWorkoutActivity(user, person, activity, day, cycle, command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreExercise()));
            when(workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                            person.getId(), day.getId(), command.exerciseId(), activity.getId()))
                    .thenReturn(false);
            when(workoutActivityRepository.save(any(WorkoutActivity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateWorkoutActivityResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutActivity> captor = ArgumentCaptor.forClass(WorkoutActivity.class);
            verify(workoutActivityRepository).save(captor.capture());
            assertThat(captor.getValue().getExerciseId()).isEqualTo(command.exerciseId());
            assertThat(captor.getValue().getSets()).isEqualTo(command.sets());
            assertThat(captor.getValue().getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(result.exerciseId()).isEqualTo(command.exerciseId());
            assertThat(result.updatedAt()).isEqualTo(updatedAt);
            verify(publisher).publish(
                    eq(AuditActionType.UPDATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_ACTIVITY), eq(activity.getId().value()),
                    any(WorkoutActivityAuditData.class), any(WorkoutActivityAuditData.class)
            );
        }
    }

    @Nested
    class AccessValidation {
        @Test
        void shouldFailWhenUserDoesNotExist() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário não encontrado.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
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
            UpdateWorkoutActivityCommand command = validUpdateCommand();
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
        void shouldFailWhenExerciseDoesNotExist() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity activity = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutActivity(user, person, activity, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício não encontrado.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenExerciseIsInactive() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity activity = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutActivity(user, person, activity, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreInactiveExercise()));
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício inativo.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenAnotherWorkoutActivityUsesExercise() {
            UpdateWorkoutActivityCommand command = validUpdateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutActivity activity = restoredWorkoutActivity();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutActivity(user, person, activity, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreExercise()));
            when(workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseIdExcludingId(
                            person.getId(), day.getId(), command.exerciseId(), activity.getId()))
                    .thenReturn(true);
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício já está vinculado a este dia de treino.");
            verify(workoutActivityRepository, never()).save(any());
        }
    }

    private void assertBlockedCycle(WorkoutCycle cycle) {
        UpdateWorkoutActivityCommand command = validUpdateCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenEditableWorkoutActivity(
                user, person, restoredWorkoutActivity(), restoredWorkoutDay(), cycle, command);
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
            UpdateWorkoutActivityCommand command) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutActivityRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.of(activity));
        when(workoutDayRepository.findByIdAndPersonId(activity.getWorkoutDayId(), person.getId()))
                .thenReturn(Optional.of(day));
        when(workoutCycleRepository.findByIdAndPersonId(day.getWorkoutCycleId(), person.getId()))
                .thenReturn(Optional.of(cycle));
    }

    private void givenFixedClock(LocalDateTime dateTime) {
        Clock fixed = Clock.fixed(Instant.from(dateTime.toInstant(ZoneOffset.UTC)), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());
    }
}
