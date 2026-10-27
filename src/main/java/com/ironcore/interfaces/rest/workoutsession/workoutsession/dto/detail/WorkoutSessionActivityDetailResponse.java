package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import io.swagger.v3.oas.annotations.media.Schema;

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
        List<WorkoutSessionSetDetailResponse> sets,
        @Schema(description = "Modo comum derivado dos sets com carga; null quando não há carga registrada.", nullable = true)
        ActualLoadMode actualLoadMode
) {
}
