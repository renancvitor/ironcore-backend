package com.ironcore.application.workoutsession.workoutsession;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WorkoutSessionAuditData(
        Long id,
        Long personId,
        Long workoutCycleId,
        Long workoutDayId,
        String workoutCycleNameSnapshot,
        String workoutDayTitleSnapshot,
        WeekDay weekDaySnapshot,
        LocalDate performedOn,
        Instant startedAt,
        Instant finishedAt,
        WorkoutSessionStatus status,
        WorkoutSessionCloseReason closeReason,
        String notes,
        List<Activity> activities
) implements LoggableData {

    public WorkoutSessionAuditData {
        activities = List.copyOf(activities);
    }

    public record Activity(Long id, Integer actualDurationMinutes, BigDecimal actualDistanceKm,
                           String notes, List<Set> sets) {
        public Activity {
            sets = List.copyOf(sets);
        }
    }

    public record Set(Long id, Integer setNumber, BigDecimal actualLoadKg, ActualLoadMode actualLoadMode,
                      Integer actualReps, String notes) {
    }

    public static WorkoutSessionAuditData from(WorkoutSession session) {
        return new WorkoutSessionAuditData(
                session.getId().value(),
                session.getPersonId().value(),
                session.getWorkoutCycleId() == null ? null : session.getWorkoutCycleId().value(),
                session.getWorkoutDayId() == null ? null : session.getWorkoutDayId().value(),
                session.getSnapshot().workoutCycleName(),
                session.getSnapshot().workoutDayTitle(),
                session.getSnapshot().weekDay(),
                session.getPerformedOn(),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getStatus(),
                session.getCloseReason(),
                session.getNotes(),
                session.getActivities().stream().map(activity -> new Activity(
                        activity.getId().value(), activity.getActualDurationMinutes(), activity.getActualDistanceKm(),
                        activity.getNotes(), activity.getSets().stream().map(set -> new Set(
                                set.getId().value(), set.getSetNumber(), set.getActualLoadKg(), set.getActualLoadMode(),
                                set.getActualReps(), set.getNotes())).toList())).toList()
        );
    }
}
