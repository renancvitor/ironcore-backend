package com.ironcore.interfaces.rest.workoutsession.dto.list;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import java.time.Instant;
import java.time.LocalDate;

public record ListWorkoutSessionsItemResponse(
        Long id,
        Long workoutCycleId,
        Long workoutDayId,
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
