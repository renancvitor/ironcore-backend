package com.ironcore.domain.workoutsession.workoutsession.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.*;
import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.*;
import static org.assertj.core.api.Assertions.*;

class WorkoutSessionTest {
    @Test
    void shouldStartWithHistoricalPrescriptionAndNoExecution() {
        WorkoutSession session = session();
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        assertThat(session.getCloseReason()).isNull();
        assertThat(session.getFinishedAt()).isNull();
        assertThat(session.getPerformedOn()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(session.getExpiresAt()).isEqualTo(EXPIRY);
        assertThat(session.getActivities()).hasSize(1);
        var activity = session.getActivities().getFirst();
        assertThat(activity.getSnapshot().plannedSets()).isEqualTo(5);
        assertThat(activity.getSnapshot().plannedTargetLoadKg()).isEqualByComparingTo("90");
        assertThat(activity.getSnapshot().activityType()).isEqualTo("STRENGTH");
        assertThat(activity.getActualDurationMinutes()).isNull();
        assertThat(activity.getActualDistanceKm()).isNull();
        assertThat(activity.getNotes()).isNull();
        assertThat(activity.getSets()).isEmpty();
        assertThatThrownBy(() -> session.getActivities().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @EnumSource(value = WorkoutStatus.class, names = "IN_PROGRESS", mode = EnumSource.Mode.EXCLUDE)
    void shouldRejectCyclesThatAreNotInProgress(WorkoutStatus status) {
        assertThatThrownBy(() -> WorkoutSession.start(OWNER, restoredWorkoutCycle(status, START_DATE, null),
                restoredWorkoutDay(), List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldRequireExistingPlanningOwnedByPerson() {
        assertThatThrownBy(() -> WorkoutSession.start(new PersonId(2L), inProgressWorkoutCycle(),
                restoredWorkoutDay(), List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> WorkoutSession.start(OWNER, inProgressWorkoutCycle(),
                com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.workoutDayWithoutId(),
                List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        var wrongDay = com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay.restore(
                new WorkoutDayId(1L), new WorkoutCycleId(2L), restoredWorkoutDay().getWeekDay(),
                "Outro ciclo", 1, LocalDateTime.now(), null);
        assertThatThrownBy(() -> WorkoutSession.start(OWNER, inProgressWorkoutCycle(), wrongDay,
                List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldRejectActivityFromAnotherDayAndDuplicateOrder() {
        var activity = restoredWorkoutActivity();
        var other = WorkoutActivity.restore(activity.getId(), new WorkoutDayId(2L), activity.getExerciseId(),
                1, null, null, null, null, null, null, null, null, null, null, activity.getCreatedAt(), null);
        assertThatThrownBy(() -> WorkoutSession.start(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(other)), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> WorkoutSession.start(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(activity), source(activity)), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldPreserveSnapshotWhenPlanningChanges() {
        var cycle = inProgressWorkoutCycle();
        var day = restoredWorkoutDay();
        var activity = restoredWorkoutActivity();
        WorkoutSession session = WorkoutSession.start(OWNER, cycle, day, List.of(source(activity)), START);
        var before = session.getActivities().getFirst().getSnapshot();
        cycle.updateCycle("Nome novo", cycle.getTrainingGoalId(), 4, cycle.getWeekStartsOn(), null, LocalDateTime.now());
        day.updateDay("Título novo", LocalDateTime.now());
        activity.updateActivity(activity.getExerciseId(), null, null, null, null, null, 20,
                null, null, null, "Prescrição alterada", LocalDateTime.now());
        assertThat(session.getSnapshot().workoutCycleName()).isEqualTo("Ciclo de hipertrofia");
        assertThat(session.getSnapshot().workoutDayTitle()).isEqualTo("Treino de membros inferiores");
        assertThat(session.getActivities().getFirst().getSnapshot()).isEqualTo(before);
    }

    @Test
    void shouldSupportMixedActivitiesAndOptionalSetsWithoutPrescription() {
        var planned = restoredWorkoutActivity();
        var cardio = WorkoutActivity.restore(new com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId(2L),
                planned.getWorkoutDayId(), planned.getExerciseId(), 3, null, null, null, null, null,
                30, new BigDecimal("2.00"), null, null, null, planned.getCreatedAt(), null);
        WorkoutSession session = WorkoutSession.start(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(planned), source(cardio)), START);
        session.updateActivityExecution(OWNER, 3, 25, new BigDecimal("2.50"), "Caminhada", START.plusSeconds(1));
        assertThat(session.getActivities().getLast().getSets()).isEmpty();
        session.recordSet(OWNER, 3, 1, null, null, null, "Pausa no percurso", START.plusSeconds(2));
        session.recordSet(OWNER, 2, 6, new BigDecimal("0.00"), ActualLoadMode.TOTAL, 10, null, START.plusSeconds(3));
        assertThat(session.getActivities().getLast().getSnapshot().plannedSets()).isNull();
        assertThat(session.getActivities().getLast().getSets()).hasSize(1);
        assertThat(session.getActivities().getFirst().getSets().getFirst().getSetNumber()).isEqualTo(6);
    }

    @Test
    void shouldCloseWithoutAnyExecutionAndAllowLaterCorrectionsWithoutReopening() {
        WorkoutSession session = session();
        session.closeManually(OWNER, START.plusSeconds(30));
        session.updateNotes(OWNER, "Relato posterior", EXPIRY.plusSeconds(1));
        session.recordSet(OWNER, 2, 1, null, null, null, "Anotação", EXPIRY.plusSeconds(2));
        session.updateActivityExecution(OWNER, 2, 20, null, "Correção", EXPIRY.plusSeconds(3));
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
        assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.MANUAL);
        assertThat(session.getFinishedAt()).isEqualTo(START.plusSeconds(30));
        assertThatThrownBy(() -> session.closeManually(OWNER, EXPIRY.plusSeconds(4)))
                .isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldExpireAtBoundaryAndRecoverAfterDowntime() {
        WorkoutSession session = session();
        assertThat(session.expireIfDue(EXPIRY.minusNanos(1))).isFalse();
        assertThat(session.expireIfDue(EXPIRY.plus(Duration.ofDays(2)))).isTrue();
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);
        assertThat(session.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
        assertThat(session.getFinishedAt()).isEqualTo(EXPIRY);
        assertThat(session.expireIfDue(EXPIRY.plus(Duration.ofDays(3)))).isFalse();
        session.updateNotes(OWNER, "Correção após expiração", EXPIRY.plus(Duration.ofDays(4)));
        assertThat(session.getStatus()).isEqualTo(WorkoutSessionStatus.CLOSED);

        WorkoutSession manualAfterCutoff = session();
        manualAfterCutoff.closeManually(OWNER, EXPIRY);
        assertThat(manualAfterCutoff.getCloseReason()).isEqualTo(WorkoutSessionCloseReason.AUTO_EXPIRED);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-10-26T02:00:00Z,2026-10-25,2026-10-26T06:00:00Z",
            "2026-10-26T03:01:00Z,2026-10-26,2026-10-27T06:00:00Z",
            "2026-10-27T05:59:00Z,2026-10-27,2026-10-28T06:00:00Z"
    })
    void shouldUseLocalStartDateRegardlessOfJvmTimezone(String instant, String date, String expiry) {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            WorkoutSession session = WorkoutSession.start(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                    List.of(), Instant.parse(instant));
            assertThat(session.getPerformedOn()).isEqualTo(LocalDate.parse(date));
            assertThat(session.getExpiresAt()).isEqualTo(Instant.parse(expiry));
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test
    void shouldRejectEveryMutationByAnotherOwner() {
        WorkoutSession session = session();
        PersonId other = new PersonId(2L);
        Instant now = START.plusSeconds(1);
        assertThatThrownBy(() -> session.closeManually(other, now)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.updateNotes(other, "Texto", now)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.updateActivityExecution(other, 2, 30, null, null, now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.recordSet(other, 2, 1, null, null, null, "Texto", now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.removeSet(other, 2, 1, now)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.changeActivityLoadMode(other, 2, ActualLoadMode.TOTAL, now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldKeepOneLoadModeAndChangeAllLoadedSetsAtomically() {
        WorkoutSession session = session();
        session.recordSet(OWNER, 2, 1, new BigDecimal("45.00"), ActualLoadMode.PER_SIDE, 10, null, START);
        session.recordSet(OWNER, 2, 2, null, null, null, "Texto", START);
        assertThatThrownBy(() -> session.recordSet(OWNER, 2, 3, new BigDecimal("50.00"),
                ActualLoadMode.TOTAL, 8, null, START)).isInstanceOf(InvalidWorkoutSessionActivityException.class);
        assertThat(session.getActivities().getFirst().getSets()).hasSize(2);
        session.changeActivityLoadMode(OWNER, 2, ActualLoadMode.TOTAL, START.plusSeconds(1));
        var sets = session.getActivities().getFirst().getSets();
        assertThat(sets.getFirst().getActualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThat(sets.getFirst().getActualLoadKg()).isEqualByComparingTo("45");
        assertThat(sets.getLast().getActualLoadMode()).isNull();
        session.recordSet(OWNER, 2, 1, new BigDecimal("50.00"), ActualLoadMode.TOTAL, 8, null, START.plusSeconds(2));
        assertThat(session.getActivities().getFirst().getSets()).hasSize(2);
        session.removeSet(OWNER, 2, 1, START.plusSeconds(3));
        assertThat(session.getActivities().getFirst().getSets()).hasSize(1);
    }

    @Test
    void shouldValidateIdentifiers() {
        for (Long value : Arrays.asList(null, 0L, -1L)) {
            assertThatThrownBy(() -> new WorkoutSessionId(value)).isInstanceOf(InvalidWorkoutSessionException.class);
        }
    }

    @Test
    void shouldRestoreHistoryWithDeletedOrigins() {
        WorkoutSession original = session();
        WorkoutSession restored = WorkoutSession.restore(new WorkoutSessionId(1L), OWNER, null, null,
                original.getSnapshot(), original.getPerformedOn(), START, EXPIRY, WorkoutSessionStatus.CLOSED,
                WorkoutSessionCloseReason.AUTO_EXPIRED, null, START, EXPIRY, 0L, List.of());
        assertThat(restored.getWorkoutDayId()).isNull();
        assertThat(restored.getSnapshot()).isEqualTo(original.getSnapshot());
    }

    @Test
    void shouldRejectInconsistentLifecycleOnRestore() {
        WorkoutSession original = session();
        assertThatThrownBy(() -> WorkoutSession.restore(new WorkoutSessionId(1L), OWNER, null, null,
                original.getSnapshot(), original.getPerformedOn(), START, null, WorkoutSessionStatus.CLOSED,
                null, null, START, null, 0L, List.of())).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> WorkoutSession.restore(new WorkoutSessionId(1L), OWNER, null, null,
                original.getSnapshot(), original.getPerformedOn(), START, EXPIRY.minusSeconds(1),
                WorkoutSessionStatus.CLOSED, WorkoutSessionCloseReason.AUTO_EXPIRED,
                null, START, EXPIRY, 0L, List.of())).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> original.updateNotes(OWNER, "Texto", START.minusSeconds(1)))
                .isInstanceOf(InvalidWorkoutSessionException.class);
    }
}
