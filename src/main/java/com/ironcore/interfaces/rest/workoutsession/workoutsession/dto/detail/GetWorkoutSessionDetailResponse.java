package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionCloseReason;
import com.ironcore.domain.workoutsession.workoutsession.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GetWorkoutSessionDetailResponse(
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
        Instant createdAt,
        Instant updatedAt,
        List<WorkoutSessionActivityDetailResponse> activities
) {
}
