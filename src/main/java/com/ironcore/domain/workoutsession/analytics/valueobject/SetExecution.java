package com.ironcore.domain.workoutsession.analytics.valueobject;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.math.BigDecimal;

public record SetExecution(
        BigDecimal loadKg,
        ActualLoadMode mode,
        Integer reps
) {
}
