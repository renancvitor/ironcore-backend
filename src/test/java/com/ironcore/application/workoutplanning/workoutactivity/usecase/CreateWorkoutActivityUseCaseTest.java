package com.ironcore.application.workoutplanning.workoutactivity.usecase;

import static com.ironcore.application.workoutplanning.workoutactivity.WorkoutActivityUseCaseTestFactory.validCreateCommand;
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
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityCommand;
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityResult;
import com.ironcore.application.workoutplanning.workoutactivity.create.CreateWorkoutActivityUseCase;
import com.ironcore.domain.exercise.exception.InvalidExerciseException;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
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
class CreateWorkoutActivityUseCaseTest {

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

    private CreateWorkoutActivityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new CreateWorkoutActivityUseCase(
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
    class SuccessfulCreation {
        @Test
        void shouldCreateWorkoutActivityAtNextPosition() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay workoutDay = restoredWorkoutDay();
            WorkoutCycle cycle = inProgressWorkoutCycle();
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 23, 12, 0);
            givenFixedClock(createdAt);
            givenEditableWorkoutDay(user, person, workoutDay, cycle, command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreExercise()));
            when(workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                            person.getId(), workoutDay.getId(), command.exerciseId()))
                    .thenReturn(false);
            when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(
                            person.getId(), workoutDay.getId()))
                    .thenReturn(List.of(restoredWorkoutActivity()));
            when(workoutActivityRepository.save(any(WorkoutActivity.class)))
                    .thenAnswer(
                            invocation ->
                                    WorkoutActivity.restore(
                                            new WorkoutActivityId(2L),
                                            workoutDay.getId(),
                                            command.exerciseId(),
                                            3,
                                            command.sets(),
                                            command.repRangeMin(),
                                            command.repRangeMax(),
                                            command.targetLoadKg(),
                                            command.targetLoadText(),
                                            command.durationMinutes(),
                                            command.distanceKm(),
                                            command.intensityText(),
                                            command.restSeconds(),
                                            command.notes(),
                                            createdAt,
                                            null));

            CreateWorkoutActivityResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutActivity> captor = ArgumentCaptor.forClass(WorkoutActivity.class);
            verify(workoutActivityRepository).save(captor.capture());
            assertThat(captor.getValue().getOrderIndex()).isEqualTo(3);
            assertThat(captor.getValue().getCreatedAt()).isEqualTo(createdAt);
            verify(publisher).publish(
                    eq(AuditActionType.CREATE), eq(user.getId().value()), eq(user.getEmail().value()),
                    eq(AuditTargetType.WORKOUT_ACTIVITY), eq(result.id().value()), isNull(),
                    any(WorkoutActivityAuditData.class)
            );
            assertThat(result.id()).isEqualTo(new WorkoutActivityId(2L));
            assertThat(result.orderIndex()).isEqualTo(3);
        }
    }

    @Nested
    class AccessValidation {
        @Test
        void shouldFailWhenUserDoesNotExist() {
            assertFailureBeforeWorkoutDayLookup(Optional.empty(), "Usuário não encontrado.");
        }

        @Test
        void shouldFailWhenUserIsInactive() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatExceptionOfType(UserInactiveException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Usuário inativo.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Pessoa não encontrada.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenWorkoutDayDoesNotBelongToPerson() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(restoredPerson()));
            when(workoutDayRepository.findByIdAndPersonId(command.workoutDayId(), user.getPersonId()))
                    .thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Dia de treino não encontrado.");
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
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutDay(user, person, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId())).thenReturn(Optional.empty());
            assertThatExceptionOfType(ResourceNotFoundException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício não encontrado.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenExerciseIsInactive() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutDay(user, person, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreInactiveExercise()));
            assertThatExceptionOfType(InvalidExerciseException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício inativo.");
            verify(workoutActivityRepository, never()).save(any());
        }

        @Test
        void shouldFailWhenExerciseIsAlreadyLinkedToWorkoutDay() {
            CreateWorkoutActivityCommand command = validCreateCommand();
            User user = activeUser();
            Person person = restoredPerson();
            WorkoutDay day = restoredWorkoutDay();
            givenEditableWorkoutDay(user, person, day, inProgressWorkoutCycle(), command);
            when(exerciseRepository.findById(command.exerciseId()))
                    .thenReturn(Optional.of(restoreExercise()));
            when(workoutActivityRepository.existsByPersonIdAndWorkoutDayIdAndExerciseId(
                            person.getId(), day.getId(), command.exerciseId()))
                    .thenReturn(true);
            assertThatExceptionOfType(OperationNotAllowedException.class)
                    .isThrownBy(() -> useCase.execute(command))
                    .withMessage("Exercício já está vinculado a este dia de treino.");
            verify(workoutActivityRepository, never()).save(any());
        }
    }

    private void assertFailureBeforeWorkoutDayLookup(Optional<User> user, String message) {
        CreateWorkoutActivityCommand command = validCreateCommand();
        when(userRepository.findById(command.actorUserId())).thenReturn(user);
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage(message);
        verify(workoutActivityRepository, never()).save(any());
    }

    private void assertBlockedCycle(WorkoutCycle cycle) {
        CreateWorkoutActivityCommand command = validCreateCommand();
        User user = activeUser();
        Person person = restoredPerson();
        givenEditableWorkoutDay(user, person, restoredWorkoutDay(), cycle, command);
        assertThatExceptionOfType(OperationNotAllowedException.class)
                .isThrownBy(() -> useCase.execute(command))
                .withMessage(
                        "Não é permitido adicionar atividades de treino de ciclos concluídos ou cancelados.");
        verify(workoutActivityRepository, never()).save(any());
    }

    private void givenEditableWorkoutDay(
            User user,
            Person person,
            WorkoutDay day,
            WorkoutCycle cycle,
            CreateWorkoutActivityCommand command) {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
        when(workoutDayRepository.findByIdAndPersonId(command.workoutDayId(), person.getId()))
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
