package com.ironcore.domain.workoutsession.analytics.service;

import com.ironcore.domain.workoutsession.analytics.valueobject.ActivityExecution;
import com.ironcore.domain.workoutsession.analytics.valueobject.CardioExecutionMetrics;
import com.ironcore.domain.workoutsession.analytics.valueobject.SetExecution;
import com.ironcore.domain.workoutsession.analytics.valueobject.StrengthExecutionMetrics;
import com.ironcore.domain.workoutsession.workoutsessionset.service.ExecutedLoadNormalizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ExecutionMetricsCalculator {

    private ExecutionMetricsCalculator() {
    }

    public static StrengthExecutionMetrics calculateStrength(List<SetExecution> sets) {
        BigDecimal maxNormalizedLoadKg = null;
        BigDecimal volumeKgReps = null;
        long loadedSetCount = 0;
        long volumeSetCount = 0;

        for (var set : sets) {
            var normalizedLoadKg = ExecutedLoadNormalizer.normalize(set.loadKg(), set.mode());

            if (normalizedLoadKg != null) {
                loadedSetCount++;

                if (set.reps() != null && set.reps() >= 1 && set.mode() != null) {
                    maxNormalizedLoadKg = maxNormalizedLoadKg == null
                            ? normalizedLoadKg : maxNormalizedLoadKg.max(normalizedLoadKg);
                    var setVolume = normalizedLoadKg.multiply(BigDecimal.valueOf(set.reps()));
                    volumeKgReps = volumeKgReps == null ? setVolume : volumeKgReps.add(setVolume);
                    volumeSetCount++;
                }
            }
        }

        return new StrengthExecutionMetrics(
                scale(maxNormalizedLoadKg),
                scale(volumeKgReps),
                sets.size(),
                loadedSetCount,
                volumeSetCount
        );
    }

    public static CardioExecutionMetrics calculateCardio(List<ActivityExecution> activities) {
        BigDecimal totalDistanceKm = null;
        Long totalDurationMinutes = null;
        BigDecimal paceDistanceKm = BigDecimal.ZERO;
        BigDecimal paceDurationMinutes = BigDecimal.ZERO;
        BigDecimal speedDistanceKm = BigDecimal.ZERO;
        BigDecimal speedDurationMinutes = BigDecimal.ZERO;
        long distanceRecordCount = 0;
        long durationRecordCount = 0;
        long paceRecordCount = 0;
        long speedRecordCount = 0;

        for (var activity : activities) {
            var distanceKm = activity.distanceKm();
            var durationMinutes = activity.durationMinutes();

            if (distanceKm != null) {
                totalDistanceKm = totalDistanceKm == null ? distanceKm : totalDistanceKm.add(distanceKm);
                distanceRecordCount++;
            }
            if (durationMinutes != null) {
                totalDurationMinutes = Math.addExact(
                        totalDurationMinutes == null ? 0L : totalDurationMinutes,
                        durationMinutes.longValue()
                );
                durationRecordCount++;
            }
            if (durationMinutes != null && durationMinutes > 0 && distanceKm != null && distanceKm.signum() > 0) {
                paceDistanceKm = paceDistanceKm.add(distanceKm);
                paceDurationMinutes = paceDurationMinutes.add(BigDecimal.valueOf(durationMinutes));
                paceRecordCount++;
            }
            if (durationMinutes != null && durationMinutes > 0 && distanceKm != null && distanceKm.signum() >= 0) {
                speedDistanceKm = speedDistanceKm.add(distanceKm);
                speedDurationMinutes = speedDurationMinutes.add(BigDecimal.valueOf(durationMinutes));
                speedRecordCount++;
            }
        }

        return new CardioExecutionMetrics(
                scale(totalDistanceKm),
                totalDurationMinutes,
                paceRecordCount == 0 ? null : paceDurationMinutes.divide(paceDistanceKm, 2, RoundingMode.HALF_UP),
                speedRecordCount == 0 ? null : speedDistanceKm.multiply(BigDecimal.valueOf(60))
                        .divide(speedDurationMinutes, 2, RoundingMode.HALF_UP),
                activities.size(),
                distanceRecordCount,
                durationRecordCount,
                paceRecordCount,
                speedRecordCount
        );
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
