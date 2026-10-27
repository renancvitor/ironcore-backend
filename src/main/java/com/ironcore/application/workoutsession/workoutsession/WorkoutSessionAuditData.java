package com.ironcore.application.workoutsession.workoutsession;

import com.ironcore.application.logging.audit.payload.LoggableData;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;

import java.time.Instant;
import java.time.LocalDate;

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
        String notes
) implements LoggableData {

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
                session.getNotes()
        );
    }
}
