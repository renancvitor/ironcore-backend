package com.ironcore.domain.workoutsession.analytics;

import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;
import com.ironcore.domain.workoutsession.workoutsessionset.model.ExecutedLoad;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ExecutionMetrics {
    private ExecutionMetrics() {}

    public record SetExecution(BigDecimal loadKg, ActualLoadMode mode, Integer reps) {}
    public record ActivityExecution(Integer durationMinutes, BigDecimal distanceKm) {}
    public record Strength(BigDecimal maxNormalizedLoadKg, BigDecimal volumeKgReps,
                           long registeredSetCount, long loadedSetCount, long volumeSetCount) {}
    public record Cardio(BigDecimal distanceKm, Long durationMinutes,
                         BigDecimal averagePaceMinPerKm, BigDecimal averageSpeedKmPerHour,
                         long activityCount, long distanceRecordCount, long durationRecordCount,
                         long paceRecordCount, long speedRecordCount) {}

    public static Strength strength(List<SetExecution> sets) {
        BigDecimal max = null;
        BigDecimal volume = null;
        long loaded = 0;
        long calculable = 0;
        for (var set : sets) {
            var load = ExecutedLoad.normalize(set.loadKg(), set.mode());
            if (load != null) {
                loaded++;
                if (set.reps() != null && set.reps() >= 1 && set.mode() != null) {
                    max = max == null ? load : max.max(load);
                    var value = load.multiply(BigDecimal.valueOf(set.reps()));
                    volume = volume == null ? value : volume.add(value);
                    calculable++;
                }
            }
        }
        return new Strength(scale(max), scale(volume), sets.size(), loaded, calculable);
    }

    public static Cardio cardio(List<ActivityExecution> activities) {
        BigDecimal distance = null;
        Long duration = null;
        BigDecimal paceDistance = BigDecimal.ZERO;
        BigDecimal paceDuration = BigDecimal.ZERO;
        BigDecimal speedDistance = BigDecimal.ZERO;
        BigDecimal speedDuration = BigDecimal.ZERO;
        long distances = 0, durations = 0, paces = 0, speeds = 0;
        for (var activity : activities) {
            var d = activity.distanceKm();
            var t = activity.durationMinutes();
            if (d != null) {
                distance = distance == null ? d : distance.add(d);
                distances++;
            }
            if (t != null) {
                duration = Math.addExact(duration == null ? 0L : duration, t.longValue());
                durations++;
            }
            if (t != null && t > 0 && d != null && d.signum() > 0) {
                paceDistance = paceDistance.add(d);
                paceDuration = paceDuration.add(BigDecimal.valueOf(t));
                paces++;
            }
            if (t != null && t > 0 && d != null && d.signum() >= 0) {
                speedDistance = speedDistance.add(d);
                speedDuration = speedDuration.add(BigDecimal.valueOf(t));
                speeds++;
            }
        }
        return new Cardio(scale(distance), duration,
                paces == 0 ? null : paceDuration.divide(paceDistance, 2, RoundingMode.HALF_UP),
                speeds == 0 ? null : speedDistance.multiply(BigDecimal.valueOf(60))
                        .divide(speedDuration, 2, RoundingMode.HALF_UP),
                activities.size(), distances, durations, paces, speeds);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
