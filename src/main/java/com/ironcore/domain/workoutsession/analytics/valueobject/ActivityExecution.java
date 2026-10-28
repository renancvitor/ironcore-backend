package com.ironcore.domain.workoutsession.analytics.valueobject;

import java.math.BigDecimal;

public record ActivityExecution(
        Integer durationMinutes,
        BigDecimal distanceKm
) {
}
