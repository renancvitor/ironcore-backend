package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WorkoutSessionActivityDetailResponse(
        Long id,
        Long workoutActivityId,
        Long exerciseId,
        String exerciseNameSnapshot,
        String activityTypeSnapshot,
        Integer orderIndexSnapshot,
        Integer plannedSetsSnapshot,
        Integer plannedRepRangeMinSnapshot,
        Integer plannedRepRangeMaxSnapshot,
        BigDecimal plannedTargetLoadKgSnapshot,
        String plannedTargetLoadTextSnapshot,
        Integer plannedDurationMinutesSnapshot,
        BigDecimal plannedDistanceKmSnapshot,
        String plannedIntensityTextSnapshot,
        Integer plannedRestSecondsSnapshot,
        String plannedNotesSnapshot,
        Integer actualDurationMinutes,
        BigDecimal actualDistanceKm,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        List<WorkoutSessionSetDetailResponse> sets
) {
}
