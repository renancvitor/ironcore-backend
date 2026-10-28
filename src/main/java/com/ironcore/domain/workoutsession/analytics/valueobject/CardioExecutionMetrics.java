package com.ironcore.domain.workoutsession.analytics.valueobject;

import java.math.BigDecimal;

public record CardioExecutionMetrics(
        BigDecimal distanceKm,
        Long durationMinutes,
        BigDecimal averagePaceMinPerKm,
        BigDecimal averageSpeedKmPerHour,
        long activityCount,
        long distanceRecordCount,
        long durationRecordCount,
        long paceRecordCount,
        long speedRecordCount
) {
}
