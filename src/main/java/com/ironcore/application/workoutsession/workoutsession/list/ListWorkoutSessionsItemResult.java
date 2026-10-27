package com.ironcore.application.workoutsession.workoutsession.list;

import com.ironcore.domain.workoutsession.workoutsession.valueobject.WorkoutSessionId;
import com.ironcore.domain.workoutplanning.workoutcycle.valueobject.WorkoutCycleId;
import com.ironcore.domain.workoutplanning.workoutday.valueobject.WorkoutDayId;
import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import java.time.Instant;
import java.time.LocalDate;

public record ListWorkoutSessionsItemResult(
        WorkoutSessionId id,
        WorkoutCycleId workoutCycleId,
        WorkoutDayId workoutDayId,
        String workoutCycleNameSnapshot,
        String workoutDayTitleSnapshot,
        WeekDay weekDaySnapshot,
        LocalDate performedOn,
        Instant startedAt,
        Instant finishedAt,
        WorkoutSessionStatus status,
        WorkoutSessionCloseReason closeReason
) {
}
