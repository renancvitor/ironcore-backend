package com.ironcore.application.workoutsession.workoutsession.persistence;

import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionCreationPort;
import com.ironcore.application.workoutsession.workoutsession.port.WorkoutSessionPlanningQueryPort;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.WorkoutSessionConflictException;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.repository.WorkoutSessionRepository;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivitySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.OWNER;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.restoredSession;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveWorkoutSessionServiceTest {

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    @Mock
    private WorkoutSessionCreationPort workoutSessionCreationPort;
    @Mock
    private WorkoutSessionPlanningQueryPort workoutSessionPlanningQueryPort;
    @InjectMocks
    private SaveWorkoutSessionService service;

    private WorkoutSession candidate;

    @BeforeEach
    void setUp() {
        candidate = WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(restoredWorkoutActivity())), START);
    }

    @Test
    void shouldLockValidateAndInsertEquivalentPrescription() {
        when(workoutSessionPlanningQueryPort.lockByDayIdAndPersonId(candidate.getWorkoutDayId(), OWNER))
                .thenReturn(Optional.of(planning(OWNER, WorkoutStatus.IN_PROGRESS,
                        List.of(activity(new BigDecimal("90.000"), new BigDecimal("6.0"))))));
        WorkoutSession saved = restoredSession(candidate);
        when(workoutSessionCreationPort.insert(any(ValidatedWorkoutSession.class))).thenAnswer(invocation -> {
            assertThat(invocation.<ValidatedWorkoutSession>getArgument(0).getSession()).isSameAs(candidate);
            return saved;
        });

        assertThat(service.save(candidate)).isSameAs(saved);
        var order = inOrder(workoutSessionPlanningQueryPort, workoutSessionCreationPort);
        order.verify(workoutSessionPlanningQueryPort).lockByDayIdAndPersonId(candidate.getWorkoutDayId(), OWNER);
        order.verify(workoutSessionCreationPort).insert(any(ValidatedWorkoutSession.class));
        verifyNoInteractions(workoutSessionRepository);
    }

    @ParameterizedTest
    @CsvSource({"90.01,6.00", "90.00,6.01"})
    void shouldRejectRealDecimalChangesBeforeInserting(BigDecimal load, BigDecimal distance) {
        when(workoutSessionPlanningQueryPort.lockByDayIdAndPersonId(candidate.getWorkoutDayId(), OWNER))
                .thenReturn(Optional.of(planning(OWNER, WorkoutStatus.IN_PROGRESS, List.of(activity(load, distance)))));

        assertThatThrownBy(() -> service.save(candidate)).isInstanceOf(WorkoutSessionConflictException.class);
        verifyNoInteractions(workoutSessionCreationPort, workoutSessionRepository);
    }

    @Test
    void shouldRejectIncompleteOrForeignOrClosedPlanning() {
        for (WorkoutSessionPlanningProjection projection : List.of(
                planning(OWNER, WorkoutStatus.IN_PROGRESS, List.of()),
                planning(new PersonId(2L), WorkoutStatus.IN_PROGRESS,
                        List.of(activity(new BigDecimal("90.00"), new BigDecimal("6.00")))),
                planning(OWNER, WorkoutStatus.COMPLETED,
                        List.of(activity(new BigDecimal("90.00"), new BigDecimal("6.00")))))) {
            when(workoutSessionPlanningQueryPort.lockByDayIdAndPersonId(candidate.getWorkoutDayId(), OWNER))
                    .thenReturn(Optional.of(projection));
            assertThatThrownBy(() -> service.save(candidate)).isInstanceOf(WorkoutSessionConflictException.class);
        }
        verifyNoInteractions(workoutSessionCreationPort, workoutSessionRepository);
    }

    @Test
    void shouldRejectUnavailablePlanningBeforeInserting() {
        when(workoutSessionPlanningQueryPort.lockByDayIdAndPersonId(candidate.getWorkoutDayId(), OWNER))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(candidate)).isInstanceOf(WorkoutSessionConflictException.class);
        verifyNoInteractions(workoutSessionCreationPort, workoutSessionRepository);
    }

    @Test
    void shouldUpdateHistoryWithoutConsultingPlanning() {
        WorkoutSession history = session();
        when(workoutSessionRepository.save(history)).thenReturn(history);
        assertThat(service.save(history)).isSameAs(history);
        verifyNoInteractions(workoutSessionPlanningQueryPort, workoutSessionCreationPort);
    }

    private WorkoutSessionPlanningProjection planning(
            PersonId owner, WorkoutStatus status, List<WorkoutSessionPlanningProjection.Activity> activities
    ) {
        return new WorkoutSessionPlanningProjection(owner, candidate.getWorkoutCycleId(), candidate.getWorkoutDayId(),
                status, candidate.getSnapshot(), activities);
    }

    private WorkoutSessionPlanningProjection.Activity activity(BigDecimal load, BigDecimal distance) {
        var source = candidate.getActivities().getFirst();
        var snapshot = source.getSnapshot();
        return new WorkoutSessionPlanningProjection.Activity(source.getWorkoutActivityId(), source.getExerciseId(),
                new WorkoutSessionActivitySnapshot(snapshot.exerciseName(), snapshot.activityType(), snapshot.orderIndex(),
                        snapshot.plannedSets(), snapshot.plannedRepRangeMin(), snapshot.plannedRepRangeMax(), load,
                        snapshot.plannedTargetLoadText(), snapshot.plannedDurationMinutes(), distance,
                        snapshot.plannedIntensityText(), snapshot.plannedRestSeconds(), snapshot.plannedNotes()));
    }
}
