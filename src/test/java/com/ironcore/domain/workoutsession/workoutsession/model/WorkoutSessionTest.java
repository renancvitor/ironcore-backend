package com.ironcore.domain.workoutsession.workoutsession.model;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutactivity.model.WorkoutActivity;
import com.ironcore.domain.workoutplanning.workoutactivity.valueobject.WorkoutActivityId;
import com.ironcore.domain.workoutplanning.workoutcycle.enums.WorkoutStatus;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory;
import com.ironcore.domain.workoutplanning.workoutday.model.WorkoutDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.exception.InvalidWorkoutSessionException;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutsession.workoutsessionactivity.exception.InvalidWorkoutSessionActivityException;
import com.ironcore.domain.workoutsession.workoutsessionactivity.valueobject.WorkoutSessionActivityId;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.ironcore.domain.workoutplanning.workoutday.WorkoutDayTestFactory.restoredWorkoutDay;
import static com.ironcore.domain.workoutplanning.workoutactivity.WorkoutActivityTestFactory.restoredWorkoutActivity;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.START_DATE;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.inProgressWorkoutCycle;
import static com.ironcore.domain.workoutplanning.workoutcycle.WorkoutCycleTestFactory.restoredWorkoutCycle;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.ACTIVITY_ID;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.EXPIRY;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.OWNER;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.START;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.restoredSession;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.session;
import static com.ironcore.domain.workoutsession.workoutsession.WorkoutSessionTestFactory.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> WorkoutSession.register(OWNER, restoredWorkoutCycle(status, START_DATE, null),
                restoredWorkoutDay(), List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldRequireExistingPlanningOwnedByPerson() {
        assertThatThrownBy(() -> WorkoutSession.register(new PersonId(2L), inProgressWorkoutCycle(),
                restoredWorkoutDay(), List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> WorkoutSession.register(OWNER, inProgressWorkoutCycle(),
                WorkoutDayTestFactory.workoutDayWithoutId(),
                List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        var wrongDay = WorkoutDay.restore(
                new WorkoutDayId(1L), new WorkoutCycleId(2L), restoredWorkoutDay().getWeekDay(),
                "Outro ciclo", 1, LocalDateTime.now(), null);
        assertThatThrownBy(() -> WorkoutSession.register(OWNER, inProgressWorkoutCycle(), wrongDay,
                List.of(), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldRejectActivityFromAnotherDayAndDuplicateOrder() {
        var activity = restoredWorkoutActivity();
        var other = WorkoutActivity.restore(activity.getId(), new WorkoutDayId(2L), activity.getExerciseId(),
                1, null, null, null, null, null, null, null, null, null, null, activity.getCreatedAt(), null);
        assertThatThrownBy(() -> WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(other)), START)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(activity), source(activity)), START)).isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldPreserveSnapshotWhenPlanningChanges() {
        var cycle = inProgressWorkoutCycle();
        var day = restoredWorkoutDay();
        var activity = restoredWorkoutActivity();
        WorkoutSession session = WorkoutSession.register(OWNER, cycle, day, List.of(source(activity)), START);
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
        var cardio = WorkoutActivity.restore(new WorkoutActivityId(2L),
                planned.getWorkoutDayId(), planned.getExerciseId(), 3, null, null, null, null, null,
                30, new BigDecimal("2.00"), null, null, null, planned.getCreatedAt(), null);
        WorkoutSession session = WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
                List.of(source(planned), source(cardio)), START);
        session = restoredSession(session);
        session.updateActivityExecution(OWNER, session.getActivities().getLast().getId(), 25, new BigDecimal("2.50"), "Caminhada", START.plusSeconds(1));
        assertThat(session.getActivities().getLast().getSets()).isEmpty();
        session.addSet(OWNER, session.getActivities().getLast().getId(), 1, null, null, null, "Pausa no percurso", START.plusSeconds(2));
        session.addSet(OWNER, ACTIVITY_ID, 6, new BigDecimal("0.00"), ActualLoadMode.TOTAL, 10, null, START.plusSeconds(3));
        assertThat(session.getActivities().getLast().getSnapshot().plannedSets()).isNull();
        assertThat(session.getActivities().getLast().getSets()).hasSize(1);
        assertThat(session.getActivities().getFirst().getSets().getFirst().getSetNumber()).isEqualTo(6);
    }

    @Test
    void shouldCloseWithoutAnyExecutionAndAllowLaterCorrectionsWithoutReopening() {
        WorkoutSession session = session();
        session.closeManually(OWNER, START.plusSeconds(30));
        session.updateNotes(OWNER, "Relato posterior", EXPIRY.plusSeconds(1));
        session.addSet(OWNER, ACTIVITY_ID, 1, null, null, null, "Anotação", EXPIRY.plusSeconds(2));
        session.updateActivityExecution(OWNER, ACTIVITY_ID, 20, null, "Correção", EXPIRY.plusSeconds(3));
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
            WorkoutSession session = WorkoutSession.register(OWNER, inProgressWorkoutCycle(), restoredWorkoutDay(),
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
        assertThatThrownBy(() -> session.updateActivityExecution(other, ACTIVITY_ID, 30, null, null, now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.addSet(other, ACTIVITY_ID, 1, null, null, null, "Texto", now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.removeSet(other, ACTIVITY_ID, new WorkoutSessionSetId(100L), now)).isInstanceOf(InvalidWorkoutSessionException.class);
        assertThatThrownBy(() -> session.changeActivityLoadMode(other, ACTIVITY_ID, ActualLoadMode.TOTAL, now))
                .isInstanceOf(InvalidWorkoutSessionException.class);
    }

    @Test
    void shouldKeepOneLoadModeAndChangeAllLoadedSetsAtomically() {
        WorkoutSession session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, new BigDecimal("45.00"), ActualLoadMode.PER_SIDE, 10, null, START);
        session.addSet(OWNER, ACTIVITY_ID, 2, null, null, null, "Texto", START);
        assertThatThrownBy(() -> session.addSet(OWNER, ACTIVITY_ID, 3, new BigDecimal("50.00"),
                ActualLoadMode.TOTAL, 8, null, START)).isInstanceOf(InvalidWorkoutSessionActivityException.class);
        assertThat(session.getActivities().getFirst().getSets()).hasSize(2);
        session.changeActivityLoadMode(OWNER, ACTIVITY_ID, ActualLoadMode.TOTAL, START.plusSeconds(1));
        var sets = session.getActivities().getFirst().getSets();
        assertThat(sets.getFirst().getActualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThat(sets.getFirst().getActualLoadKg()).isEqualByComparingTo("45");
        assertThat(sets.getLast().getActualLoadMode()).isNull();
        WorkoutSession restored = restoredSession(session);
        restored.updateSet(OWNER, ACTIVITY_ID, restored.getActivities().getFirst().getSets().getFirst().getId(),
                new BigDecimal("50.00"), ActualLoadMode.TOTAL, 8, null, START.plusSeconds(2));
        assertThat(session.getActivities().getFirst().getSets()).hasSize(2);
        restored.removeSet(OWNER, ACTIVITY_ID, restored.getActivities().getFirst().getSets().getFirst().getId(), START.plusSeconds(3));
        assertThat(restored.getActivities().getFirst().getSets()).hasSize(1);
    }

    @Test
    void shouldUseActivityIdRatherThanHistoricalOrder() {
        WorkoutSession session = session();
        assertThat(session.getActivities().getFirst().getSnapshot().orderIndex()).isEqualTo(2);
        assertThatThrownBy(() -> session.updateActivityExecution(OWNER, new WorkoutSessionActivityId(2L),
                20, null, null, START)).isInstanceOf(InvalidWorkoutSessionException.class);
        session.updateActivityExecution(OWNER, ACTIVITY_ID, 20, null, null, START);
        assertThat(session.getActivities().getFirst().getActualDurationMinutes()).isEqualTo(20);
    }

    @Test
    void shouldRestoreCreationTimestampDifferentFromStart() {
        WorkoutSession original = session();
        WorkoutSession restored = WorkoutSession.restore(original.getId(), OWNER, original.getWorkoutCycleId(),
                original.getWorkoutDayId(), original.getSnapshot(), original.getPerformedOn(), START, null,
                original.getStatus(), null, null, START.plusSeconds(1), null, 0L, original.getActivities());
        assertThat(restored.getCreatedAt()).isEqualTo(START.plusSeconds(1));
        assertThat(restored.getStartedAt()).isEqualTo(START);
    }

    @Test
    void shouldUseSetIdRatherThanSetNumberAndPreserveEntityOnUpdate() {
        WorkoutSession session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, new BigDecimal("45.00"), ActualLoadMode.TOTAL, 10, null, START);
        WorkoutSession restored = restoredSession(session);
        var set = restored.getActivities().getFirst().getSets().getFirst();
        assertThatThrownBy(() -> restored.updateSet(OWNER, ACTIVITY_ID, new WorkoutSessionSetId(1L),
                new BigDecimal("50.00"), ActualLoadMode.TOTAL, 8, null, START.plusSeconds(1)))
                .isInstanceOf(InvalidWorkoutSessionActivityException.class);
        restored.updateSet(OWNER, ACTIVITY_ID, set.getId(), new BigDecimal("50.00"),
                ActualLoadMode.TOTAL, 8, null, START.plusSeconds(1));
        assertThat(restored.getActivities().getFirst().getSets().getFirst()).isSameAs(set);
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("50");
        assertThat(set.getSetNumber()).isEqualTo(1);
    }

    @Test
    void shouldRejectLoadModeMismatchBeforeChangingSet() {
        WorkoutSession session = session();
        session.addSet(OWNER, ACTIVITY_ID, 1, new BigDecimal("45.00"), ActualLoadMode.TOTAL, 10, null, START);
        session.addSet(OWNER, ACTIVITY_ID, 2, new BigDecimal("45.00"), ActualLoadMode.TOTAL, 10, null, START);
        WorkoutSession restored = restoredSession(session);
        var set = restored.getActivities().getFirst().getSets().getFirst();
        assertThatThrownBy(() -> restored.updateSet(OWNER, ACTIVITY_ID, set.getId(),
                new BigDecimal("50.00"), ActualLoadMode.PER_SIDE, 8, "Inválido", START.plusSeconds(1)))
                .isInstanceOf(InvalidWorkoutSessionActivityException.class);
        assertThat(set.getActualLoadKg()).isEqualByComparingTo("45");
        assertThat(set.getActualLoadMode()).isEqualTo(ActualLoadMode.TOTAL);
        assertThat(set.getNotes()).isNull();
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
