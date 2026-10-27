package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.domain.person.model.Person;
import com.ironcore.domain.person.repository.PersonRepository;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.model.WorkoutSessionActivity;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.person.PersonTestFactory.restoredPerson;
import static com.ironcore.domain.user.UserTestFactory.activeUser;
import static com.ironcore.domain.user.UserTestFactory.inactiveUser;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWorkoutSessionDetailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    private GetWorkoutSessionDetailUseCase useCase;
    private final User user = User.restore(new UserId(77L), "Renan", restoredPerson().getId(),
            activeUser().getEmail(), activeUser().getPasswordHash(), false, true, activeUser().getCreatedAt(), null);
    private final Person person = restoredPerson();
    private final GetWorkoutSessionDetailCommand command = new GetWorkoutSessionDetailCommand(
            user.getId(), new WorkoutSessionId(1L));

    @BeforeEach
    void setUp() {
        useCase = new GetWorkoutSessionDetailUseCase(userRepository, personRepository, workoutSessionRepository);
    }

    @Nested
    class SuccessfulDetail {

        @Test
        void shouldReturnHistoricalSnapshotsAndRecordedExecution() {
            givenPerson();
            WorkoutSession history = session();
            history.addSet(person.getId(), ACTIVITY_ID, 1, new BigDecimal("42.50"), ActualLoadMode.PER_SIDE,
                    8, "Série registrada", START.plusSeconds(1));
            history.addSet(person.getId(), ACTIVITY_ID, 2, null, null, null,
                    "Somente relato", START.plusSeconds(2));
            history.updateActivityExecution(person.getId(), ACTIVITY_ID, 20, new BigDecimal("2.25"),
                    "Execução registrada", START.plusSeconds(3));
            history.updateNotes(person.getId(), "Relato da sessão", START.plusSeconds(4));
            history.closeManually(person.getId(), START.plusSeconds(60));
            history = restoredSession(history);
            when(workoutSessionRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.of(history));

            WorkoutSessionDetailResult result = useCase.execute(command);

            assertThat(result.id()).isEqualTo(history.getId());
            assertThat(result.personId()).isEqualTo(person.getId());
            assertThat(result.workoutCycleId()).isEqualTo(history.getWorkoutCycleId());
            assertThat(result.workoutDayId()).isEqualTo(history.getWorkoutDayId());
            assertThat(result.workoutCycleNameSnapshot()).isEqualTo(history.getSnapshot().workoutCycleName());
            assertThat(result.workoutDayTitleSnapshot()).isEqualTo(history.getSnapshot().workoutDayTitle());
            assertThat(result.weekDaySnapshot()).isEqualTo(history.getSnapshot().weekDay());
            assertThat(result.performedOn()).isEqualTo(history.getPerformedOn());
            assertThat(result.startedAt()).isEqualTo(START);
            assertThat(result.finishedAt()).isEqualTo(START.plusSeconds(60));
            assertThat(result.status()).isEqualTo(WorkoutSessionStatus.CLOSED);
            assertThat(result.closeReason()).isEqualTo(WorkoutSessionCloseReason.MANUAL);
            assertThat(result.notes()).isEqualTo("Relato da sessão");
            assertThat(result.createdAt()).isEqualTo(history.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(history.getUpdatedAt());
            assertThat(result.activities()).hasSize(1);
            var activity = result.activities().getFirst();
            var source = history.getActivities().getFirst();
            assertThat(activity.id()).isEqualTo(source.getId());
            assertThat(activity.workoutActivityId()).isEqualTo(source.getWorkoutActivityId());
            assertThat(activity.exerciseId()).isEqualTo(source.getExerciseId());
            assertThat(activity.exerciseNameSnapshot()).isEqualTo(source.getSnapshot().exerciseName());
            assertThat(activity.activityTypeSnapshot()).isEqualTo(source.getSnapshot().activityType());
            assertThat(activity.orderIndexSnapshot()).isEqualTo(source.getSnapshot().orderIndex());
            assertThat(activity.plannedSetsSnapshot()).isEqualTo(source.getSnapshot().plannedSets());
            assertThat(activity.plannedRepRangeMinSnapshot()).isEqualTo(source.getSnapshot().plannedRepRangeMin());
            assertThat(activity.plannedRepRangeMaxSnapshot()).isEqualTo(source.getSnapshot().plannedRepRangeMax());
            assertThat(activity.plannedTargetLoadKgSnapshot())
                    .isEqualByComparingTo(source.getSnapshot().plannedTargetLoadKg());
            assertThat(activity.plannedTargetLoadTextSnapshot())
                    .isEqualTo(source.getSnapshot().plannedTargetLoadText());
            assertThat(activity.plannedDurationMinutesSnapshot())
                    .isEqualTo(source.getSnapshot().plannedDurationMinutes());
            assertThat(activity.plannedDistanceKmSnapshot())
                    .isEqualByComparingTo(source.getSnapshot().plannedDistanceKm());
            assertThat(activity.plannedIntensityTextSnapshot())
                    .isEqualTo(source.getSnapshot().plannedIntensityText());
            assertThat(activity.plannedRestSecondsSnapshot()).isEqualTo(source.getSnapshot().plannedRestSeconds());
            assertThat(activity.plannedNotesSnapshot()).isEqualTo(source.getSnapshot().plannedNotes());
            assertThat(activity.actualDurationMinutes()).isEqualTo(20);
            assertThat(activity.actualDistanceKm()).isEqualByComparingTo("2.25");
            assertThat(activity.notes()).isEqualTo("Execução registrada");
            assertThat(activity.createdAt()).isEqualTo(source.getCreatedAt());
            assertThat(activity.updatedAt()).isEqualTo(source.getUpdatedAt());
            assertThat(activity.sets()).hasSize(2);
            var set = activity.sets().getFirst();
            assertThat(set.id()).isEqualTo(source.getSets().getFirst().getId());
            assertThat(set.setNumber()).isEqualTo(1);
            assertThat(set.actualLoadKg()).isEqualByComparingTo("42.50");
            assertThat(set.actualLoadMode()).isEqualTo(ActualLoadMode.PER_SIDE);
            assertThat(set.actualReps()).isEqualTo(8);
            assertThat(set.notes()).isEqualTo("Série registrada");
            assertThat(set.createdAt()).isEqualTo(START.plusSeconds(1));
            assertThat(set.updatedAt()).isNull();
            var notesOnly = activity.sets().getLast();
            assertThat(notesOnly.actualLoadKg()).isNull();
            assertThat(notesOnly.actualLoadMode()).isNull();
            assertThat(notesOnly.actualReps()).isNull();
            assertThat(notesOnly.notes()).isEqualTo("Somente relato");
            verify(workoutSessionRepository, never()).save(any());
        }

        @Test
        void shouldReturnSnapshotEvenWhenAllOriginReferencesAreNull() {
            givenPerson();
            WorkoutSession original = session();
            var source = original.getActivities().getFirst();
            WorkoutSessionActivity historicalActivity = WorkoutSessionActivity.restore(source.getId(), null, null,
                    source.getSnapshot(), null, null, null, source.getCreatedAt(), null, List.of());
            WorkoutSession history = WorkoutSession.restore(original.getId(), original.getPersonId(), null, null,
                    original.getSnapshot(), original.getPerformedOn(), original.getStartedAt(), null,
                    original.getStatus(), null, null, original.getCreatedAt(), null, original.getVersion(),
                    List.of(historicalActivity));
            when(workoutSessionRepository.findByIdAndPersonId(command.id(), person.getId()))
                    .thenReturn(Optional.of(history));

            WorkoutSessionDetailResult result = useCase.execute(command);

            assertThat(result.workoutCycleId()).isNull();
            assertThat(result.workoutDayId()).isNull();
            assertThat(result.workoutCycleNameSnapshot()).isEqualTo(original.getSnapshot().workoutCycleName());
            assertThat(result.activities().getFirst().workoutActivityId()).isNull();
            assertThat(result.activities().getFirst().exerciseId()).isNull();
            assertThat(result.activities().getFirst().exerciseNameSnapshot())
                    .isEqualTo(source.getSnapshot().exerciseName());
            assertThat(result.activities().getFirst().sets()).isEmpty();
            verify(workoutSessionRepository, never()).save(any());
        }
    }

    @Nested
    class AccessValidation {

        @Test
        void shouldRejectMissingUser() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuário não encontrado.");
            verifyNoInteractions(personRepository, workoutSessionRepository);
        }

        @Test
        void shouldRejectInactiveUser() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(inactiveUser()));
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UserInactiveException.class);
            verifyNoInteractions(personRepository, workoutSessionRepository);
        }

        @Test
        void shouldRejectMissingPerson() {
            when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
            when(personRepository.findById(user.getPersonId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Pessoa não encontrada.");
            verifyNoInteractions(workoutSessionRepository);
        }

        @Test
        void shouldRejectMissingSession() {
            assertSessionNotFound();
        }

        @Test
        void shouldRejectSessionBelongingToAnotherPersonThroughOwnershipQuery() {
            assertSessionNotFound();
        }
    }

    private void assertSessionNotFound() {
        givenPerson();
        when(workoutSessionRepository.findByIdAndPersonId(command.id(), person.getId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sessão de treino não encontrada.");
        verify(workoutSessionRepository).findByIdAndPersonId(command.id(), person.getId());
        verify(workoutSessionRepository, never()).save(any());
    }

    private void givenPerson() {
        when(userRepository.findById(command.actorUserId())).thenReturn(Optional.of(user));
        when(personRepository.findById(user.getPersonId())).thenReturn(Optional.of(person));
    }
}
