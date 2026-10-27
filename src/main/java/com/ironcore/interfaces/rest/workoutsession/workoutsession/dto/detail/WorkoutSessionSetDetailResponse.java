package com.ironcore.interfaces.rest.workoutsession.workoutsession.dto.detail;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutSessionSetDetailResponse(
        Long id,
        Integer setNumber,
        BigDecimal actualLoadKg,
        ActualLoadMode actualLoadMode,
        Integer actualReps,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
