package com.ironcore.application.workoutsession.workoutsession.detail;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.model.WorkoutSessionSet;
import com.ironcore.domain.workoutsession.workoutsessionset.valueobject.WorkoutSessionSetId;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkoutSessionSetDetailResult(
        WorkoutSessionSetId id,
        Integer setNumber,
        BigDecimal actualLoadKg,
        ActualLoadMode actualLoadMode,
        Integer actualReps,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal normalizedLoadKg
) {

    public static WorkoutSessionSetDetailResult from(WorkoutSessionSet set) {
        return new WorkoutSessionSetDetailResult(
                set.getId(),
                set.getSetNumber(),
                set.getActualLoadKg(),
                set.getActualLoadMode(),
                set.getActualReps(),
                set.getNotes(),
                set.getCreatedAt(),
                set.getUpdatedAt(),
                set.getNormalizedLoadKg()
        );
    }
}
