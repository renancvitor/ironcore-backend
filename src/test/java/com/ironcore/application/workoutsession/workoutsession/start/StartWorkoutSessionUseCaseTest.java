package com.ironcore.application.workoutsession.workoutsession.start;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.logging.audit.port.AuditLogPublisher;
import com.ironcore.application.workoutsession.workoutsession.WorkoutSessionAuditData;
import com.ironcore.domain.activitytype.model.ActivityType;
import com.ironcore.domain.activitytype.repository.ActivityTypeRepository;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeCode;
import com.ironcore.domain.activitytype.valueobject.ActivityTypeId;
import com.ironcore.domain.exercise.model.Exercise;
import com.ironcore.domain.exercise.repository.ExerciseRepository;
import com.ironcore.domain.exercise.valueobject.ExerciseId;
import com.ironcore.domain.logging.audit.enums.AuditActionType;
import com.ironcore.domain.logging.audit.enums.AuditTargetType;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.repository.WorkoutActivityRepository;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.model.WorkoutCycle;
import com.ironcore.domain.workoutplanning.workoutcycle.repository.WorkoutCycleRepository;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.repository.WorkoutDayRepository;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.activitytype.ActivityTypeTestFactory.restoreActivityType;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreExercise;
import static com.ironcore.domain.exercise.ExerciseTestFactory.restoreInactiveExercise;
import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.restoredWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.restoredSession;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartWorkoutSessionUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutCycleRepository workoutCycleRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutActivityRepository workoutActivityRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private Clock clock;

    @Mock
    private AuditLogPublisher publisher;

    private StartWorkoutSessionUseCase useCase;
    private final User user = User.restore(new UserId(77L), "Renan", restoredPerson().getId(),
            activeUser().getEmail(), activeUser().getPasswordHash(), false, true, activeUser().getCreatedAt(), null);
    private final Person person = restoredPerson();
    private final WorkoutCycle cycle = inProgressWorkoutCycle();
    private final WorkoutDay day = restoredWorkoutDay();
    private final WorkoutActivity activity = restoredWorkoutActivity();
    private final StartWorkoutSessionCommand command = new StartWorkoutSessionCommand(
            user.getId(), cycle.getId(), day.getId());

    @BeforeEach
    void setUp() {
        useCase = new StartWorkoutSessionUseCase(
                userRepository, personRepository, workoutCycleRepository, workoutDayRepository,
                workoutActivityRepository, exerciseRepository, activityTypeRepository,
                workoutSessionRepository, clock, publisher);
    }

    @Nested
    class SuccessfulStart {

        @Test
        void shouldCaptureCompletePrescriptionWithoutRecordingExecution() {
            givenPlanning(cycle, day, List.of(activity));
            Exercise exercise = restoreExercise();
            ActivityType type = restoreActivityType();
            givenCatalog(exercise, type);
            when(clock.instant()).thenReturn(START);
            when(workoutSessionRepository.save(any()))
                    .thenAnswer(invocation -> restoredSession(invocation.getArgument(0)));

            StartWorkoutSessionResult result = useCase.execute(command);

            ArgumentCaptor<WorkoutSession> captor = ArgumentCaptor.forClass(WorkoutSession.class);
            verify(workoutSessionRepository).save(captor.capture());
            WorkoutSession candidate = captor.getValue();
            assertThat(candidate.getId()).isNull();
            assertThat(candidate.getPersonId()).isEqualTo(person.getId());
            assertThat(candidate.getWorkoutCycleId()).isEqualTo(cycle.getId());
            assertThat(candidate.getWorkoutDayId()).isEqualTo(day.getId());
            assertThat(candidate.getSnapshot().workoutCycleName()).isEqualTo(cycle.getName());
            assertThat(candidate.getSnapshot().workoutDayTitle()).isEqualTo(day.getTitle());
            assertThat(candidate.getSnapshot().weekDay()).isEqualTo(day.getWeekDay());
            assertThat(candidate.getStartedAt()).isEqualTo(START);
            assertThat(candidate.getPerformedOn()).isEqualTo(LocalDate.of(2026, 10, 26));
            assertThat(candidate.getFinishedAt()).isNull();
            assertThat(candidate.getCloseReason()).isNull();
            assertThat(candidate.getNotes()).isNull();
            assertThat(candidate.getActivities()).hasSize(1);
            var captured = candidate.getActivities().getFirst();
            assertThat(captured.getWorkoutActivityId()).isEqualTo(activity.getId());
            assertThat(captured.getExerciseId()).isEqualTo(exercise.getId());
            assertThat(captured.getSnapshot().exerciseName()).isEqualTo(exercise.getName());
            assertThat(captured.getSnapshot().activityType()).isEqualTo(type.getCode().value());
            assertThat(captured.getSnapshot().orderIndex()).isEqualTo(activity.getOrderIndex());
            assertThat(captured.getSnapshot().plannedSets()).isEqualTo(activity.getSets());
            assertThat(captured.getSnapshot().plannedRepRangeMin()).isEqualTo(activity.getRepRangeMin());
            assertThat(captured.getSnapshot().plannedRepRangeMax()).isEqualTo(activity.getRepRangeMax());
            assertThat(captured.getSnapshot().plannedTargetLoadKg()).isEqualByComparingTo(activity.getTargetLoadKg());
            assertThat(captured.getSnapshot().plannedTargetLoadText()).isEqualTo(activity.getTargetLoadText());
            assertThat(captured.getSnapshot().plannedDurationMinutes()).isEqualTo(activity.getDurationMinutes());
            assertThat(captured.getSnapshot().plannedDistanceKm()).isEqualByComparingTo(activity.getDistanceKm());
            assertThat(captured.getSnapshot().plannedIntensityText()).isEqualTo(activity.getIntensityText());
            assertThat(captured.getSnapshot().plannedRestSeconds()).isEqualTo(activity.getRestSeconds());
            assertThat(captured.getSnapshot().plannedNotes()).isEqualTo(activity.getNotes());
            assertThat(captured.getSets()).isEmpty();
            assertThat(captured.getActualDurationMinutes()).isNull();
            assertThat(captured.getActualDistanceKm()).isNull();
            assertThat(captured.getNotes()).isNull();
            assertThat(captured.getCreatedAt()).isEqualTo(START);
            assertThat(result.id()).isEqualTo(restoredSession(candidate).getId());
            assertThat(result.workoutCycleId()).isEqualTo(cycle.getId());
            assertThat(result.workoutDayId()).isEqualTo(day.getId());
            assertThat(result.performedOn()).isEqualTo(candidate.getPerformedOn());
            assertThat(result.startedAt()).isEqualTo(START);
            assertThat(result.status()).isEqualTo(candidate.getStatus());
            assertThat(result.createdAt()).isEqualTo(START);

            WorkoutSession saved = restoredSession(candidate);
            verify(publisher).publish(AuditActionType.CREATE, user.getId().value(), user.getEmail().value(),
                    AuditTargetType.WORKOUT_SESSION, saved.getId().value(), null, WorkoutSessionAuditData.from(saved));
            var order = inOrder(userRepository, personRepository, workoutCycleRepository, workoutDayRepository,
                    workoutSessionRepository, publisher);
            order.verify(userRepository).findById(command.actorUserId());
            order.verify(personRepository).findById(user.getPersonId());
            order.verify(workoutCycleRepository).findByIdAndPersonId(command.workoutCycleId(), person.getId());
            order.verify(workoutDayRepository).findByIdAndPersonId(command.workoutDayId(), person.getId());
            order.verify(workoutSessionRepository).save(any());
            order.verify(publisher).publish(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void shouldCaptureMixedActivitiesIncludingActivityWithoutPlannedSets() {
            Exercise strength = restoreExercise();
            ActivityType cardioType = ActivityType.restore(new ActivityTypeId(2L), new ActivityTypeCode("CARDIO"),
                    "Cardio", true, 20);
            Exercise cardio = Exercise.restore(new ExerciseId(2L), "Caminhada", strength.getEquipmentTypeId(),
                    cardioType.getId(), false, false, null, true, strength.getCreatedAt(), null);
            WorkoutActivity withoutSets = WorkoutActivity.restore(
                    new WorkoutActivityId(2L), day.getId(), cardio.getId(), 1,
                    null, null, null, null, null, 30, new BigDecimal("3.00"), "Leve", null,
                    "Prescrição sem séries", activity.getCreatedAt(), null);
            givenPlanning(cycle, day, List.of(activity, withoutSets));
            givenCatalog(strength, restoreActivityType());
            when(exerciseRepository.findById(cardio.getId())).thenReturn(Optional.of(cardio));
            when(activityTypeRepository.findById(cardioType.getId())).thenReturn(Optional.of(cardioType));
            when(clock.instant()).thenReturn(START);
            when(workoutSessionRepository.save(any()))
                    .thenAnswer(invocation -> restoredSession(invocation.getArgument(0)));

            useCase.execute(command);

            ArgumentCaptor<WorkoutSession> captor = ArgumentCaptor.forClass(WorkoutSession.class);
            verify(workoutSessionRepository).save(captor.capture());
            assertThat(captor.getValue().getActivities()).hasSize(2).allSatisfy(captured -> {
                assertThat(captured.getSets()).isEmpty();
                assertThat(captured.getActualDurationMinutes()).isNull();
                assertThat(captured.getActualDistanceKm()).isNull();
                assertThat(captured.getNotes()).isNull();
            });
            var first = captor.getValue().getActivities().getFirst();
            assertThat(first.getWorkoutActivityId()).isEqualTo(withoutSets.getId());
            assertThat(first.getSnapshot().exerciseName()).isEqualTo("Caminhada");
            assertThat(first.getSnapshot().activityType()).isEqualTo("CARDIO");
            assertThat(first.getSnapshot().plannedSets()).isNull();
            assertThat(first.getSnapshot().plannedDurationMinutes()).isEqualTo(30);
            assertThat(first.getSnapshot().plannedDistanceKm()).isEqualByComparingTo("3.00");
        }

        @Test
        void shouldCaptureExistingPlanningEvenWhenCatalogIsInactive() {
            givenPlanning(cycle, day, List.of(activity));
            ActivityType type = restoreActivityType();
            givenCatalog(restoreInactiveExercise(), ActivityType.restore(
                    type.getId(), type.getCode(), type.getDisplayName(), false, type.getSortOrder()));
            when(clock.instant()).thenReturn(START);
            when(workoutSessionRepository.save(any()))
                    .thenAnswer(invocation -> restoredSession(invocation.getArgument(0)));

            assertThat(useCase.execute(command).id()).isNotNull();
        }

        @Test
        void shouldAllowNewSessionForSameDayWhenPreviousSessionIsClosed() {
            WorkoutSession previous =
                    com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session();
            previous.closeManually(person.getId(), START.plusSeconds(10));
            givenPlanning(cycle, day, List.of(activity));
            givenCatalog(restoreExercise(), restoreActivityType());
            when(clock.instant()).thenReturn(START.plusSeconds(20));
            when(workoutSessionRepository.save(any())).thenAnswer(invocation -> {
                WorkoutSession candidate = invocation.getArgument(0);
                assertThat(candidate.getId()).isNull();
                assertThat(candidate.getWorkoutDayId()).isEqualTo(previous.getWorkoutDayId());
                assertThat(candidate.getStartedAt()).isAfter(previous.getFinishedAt());
                return restoredSession(candidate);
            });

            assertThat(useCase.execute(command).startedAt()).isEqualTo(START.plusSeconds(20));
            verify(workoutSessionRepository, never()).findInProgressByPersonId(any());
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldRejectMissingUser() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuário não encontrado.");
            verifyNoInteractions(personRepository, workoutCycleRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectInactiveUser() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UserInactiveException.class);
            verifyNoInteractions(personRepository, workoutCycleRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectMissingPerson() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Pessoa não encontrada.");
            verifyNoInteractions(workoutCycleRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectMissingOrForeignCycle() {
            givenPerson();
            when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId()))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Ciclo de treino não encontrado.");
            verifyNoInteractions(workoutDayRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectMissingOrForeignDay() {
            givenPerson();
            when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId()))
                    .thenReturn(Optional.of(cycle));
            when(workoutDayRepository.findByIdAndPersonId(command.workoutDayId(), person.getId()))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Dia de treino não encontrado.");
            verifyNoInteractions(workoutActivityRepository, workoutSessionRepository, publisher);
        }
    }

    @Nested
    class PlanningValidation {

        @ParameterizedTest
        @EnumSource(value = WorkoutStatus.class, names = {"NOT_STARTED", "COMPLETED", "CANCELLED"})
        void shouldRejectCycleOutsideInProgress(WorkoutStatus status) {
            givenPlanning(restoredWorkoutCycle(status, null, null), day, List.of(activity));
            givenCatalog(restoreExercise(), restoreActivityType());
            when(clock.instant()).thenReturn(START);
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidWorkoutSessionException.class)
                    .hasMessage("Somente um ciclo em andamento permite iniciar sessão.");
            verifyNoInteractions(workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectDayFromAnotherOwnedCycle() {
            WorkoutDay otherDay = WorkoutDay.restore(day.getId(), new WorkoutCycleId(2L), day.getWeekDay(),
                    day.getTitle(), day.getSortOrder(), day.getCreatedAt(), day.getUpdatedAt());
            givenPlanning(cycle, otherDay, List.of(activity));
            givenCatalog(restoreExercise(), restoreActivityType());
            when(clock.instant()).thenReturn(START);
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidWorkoutSessionException.class)
                    .hasMessage("Planejamento de origem não pertence à pessoa ou ao ciclo.");
            verifyNoInteractions(workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectDayWithoutActivities() {
            givenPlanning(cycle, day, List.of());
            when(clock.instant()).thenReturn(START);
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidWorkoutSessionException.class)
                    .hasMessage("O dia de treino deve possuir pelo menos uma atividade planejada.");
            verifyNoInteractions(exerciseRepository, activityTypeRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectMissingExercise() {
            givenPlanning(cycle, day, List.of(activity));
            when(exerciseRepository.findById(activity.getExerciseId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Exercício não encontrado.");
            verifyNoInteractions(activityTypeRepository, workoutSessionRepository, publisher);
        }

        @Test
        void shouldRejectMissingActivityType() {
            givenPlanning(cycle, day, List.of(activity));
            Exercise exercise = restoreExercise();
            when(exerciseRepository.findById(activity.getExerciseId())).thenReturn(Optional.of(exercise));
            when(activityTypeRepository.findById(exercise.getActivityTypeId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Tipo de atividade não encontrado.");
            verifyNoInteractions(workoutSessionRepository, publisher);
        }

        @Test
        void shouldPropagateOpenSessionConflictWithoutPublishingAudit() {
            assertSaveConflict("Já existe uma sessão em andamento para a pessoa.");
        }

        @Test
        void shouldPropagateConcurrentPlanningChangeWithoutPublishingAudit() {
            assertSaveConflict("Prescrição foi alterada. Recarregue a ficha.");
        }
    }

    private void assertSaveConflict(String message) {
        givenPlanning(cycle, day, List.of(activity));
        givenCatalog(restoreExercise(), restoreActivityType());
        when(clock.instant()).thenReturn(START);
        WorkoutSessionConflictException conflict = new WorkoutSessionConflictException(message);
        when(workoutSessionRepository.save(any())).thenThrow(conflict);
        assertThatThrownBy(() -> useCase.execute(command)).isSameAs(conflict);
        verifyNoInteractions(publisher);
    }

    private void givenPerson() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
    }

    private void givenPlanning(WorkoutCycle plannedCycle, WorkoutDay plannedDay, List<WorkoutActivity> activities) {
        givenPerson();
        when(workoutCycleRepository.findByIdAndPersonId(command.workoutCycleId(), person.getId()))
                .thenReturn(Optional.of(plannedCycle));
        when(workoutDayRepository.findByIdAndPersonId(command.workoutDayId(), person.getId()))
                .thenReturn(Optional.of(plannedDay));
        when(workoutActivityRepository.findByPersonIdAndWorkoutDayId(person.getId(), plannedDay.getId()))
                .thenReturn(activities);
    }

    private void givenCatalog(Exercise exercise, ActivityType type) {
        when(exerciseRepository.findById(activity.getExerciseId())).thenReturn(Optional.of(exercise));
        when(activityTypeRepository.findById(exercise.getActivityTypeId())).thenReturn(Optional.of(type));
    }
}
