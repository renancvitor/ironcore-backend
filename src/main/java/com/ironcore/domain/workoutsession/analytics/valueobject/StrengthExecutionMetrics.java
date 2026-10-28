package com.ironcore.domain.workoutsession.analytics.valueobject;

import java.math.BigDecimal;

public record StrengthExecutionMetrics(
        BigDecimal maxNormalizedLoadKg,
        BigDecimal volumeKgReps,
        long registeredSetCount,
        long loadedSetCount,
        long volumeSetCount
) {
}
