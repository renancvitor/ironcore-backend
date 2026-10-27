package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.model.WorkoutSession;
import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record WorkoutSessionDetailResult(
        WorkoutSessionId id,
        PersonId personId,
        WorkoutCycleId workoutCycleId,
        WorkoutDayId workoutDayId,
        String workoutCycleNameSnapshot,
        String workoutDayTitleSnapshot,
        WeekDay weekDaySnapshot,
        LocalDate performedOn,
        Instant startedAt,
        Instant finishedAt,
        WorkoutSessionStatus status,
        WorkoutSessionCloseReason closeReason,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        List<WorkoutSessionActivityDetailResult> activities
) {

    public static WorkoutSessionDetailResult from(WorkoutSession session) {
        return new WorkoutSessionDetailResult(
                session.getId(),
                session.getPersonId(),
                session.getWorkoutCycleId(),
                session.getWorkoutDayId(),
                session.getSnapshot().workoutCycleName(),
                session.getSnapshot().workoutDayTitle(),
                session.getSnapshot().weekDay(),
                session.getPerformedOn(),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getStatus(),
                session.getCloseReason(),
                session.getNotes(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                session.getActivities().stream().map(WorkoutSessionActivityDetailResult::from).toList()
        );
    }
}
