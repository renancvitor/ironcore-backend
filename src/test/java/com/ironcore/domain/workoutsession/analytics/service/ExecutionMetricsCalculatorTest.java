package com.ironcore.domain.workoutsession.analytics.service;

import com.ironcore.domain.workoutsession.analytics.valueobject.ActivityExecution;
import com.ironcore.domain.workoutsession.analytics.valueobject.SetExecution;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.ironcore.domain.workoutsession.analytics.service.ExecutionMetricsCalculator.calculateCardio;
import static com.ironcore.domain.workoutsession.analytics.service.ExecutionMetricsCalculator.calculateStrength;
import static com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.PER_SIDE;
import static com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode.TOTAL;
import static org.assertj.core.api.Assertions.assertThat;

class ExecutionMetricsCalculatorTest {

    @Test
    void shouldNormalizeAndSumOnlyCalculableSetsIncludingRecordedZero() {
        var value = calculateStrength(List.of(
                new SetExecution(new BigDecimal("20.10"), PER_SIDE, 3),
                new SetExecution(new BigDecimal("50"), TOTAL, null),
                new SetExecution(null, null, 10),
                new SetExecution(null, null, null),
                new SetExecution(BigDecimal.ZERO, TOTAL, 8)));
        assertThat(value.maxNormalizedLoadKg()).isEqualByComparingTo("40.20");
        assertThat(value.volumeKgReps()).isEqualByComparingTo("120.60");
        assertThat(value.registeredSetCount()).isEqualTo(5);
        assertThat(value.loadedSetCount()).isEqualTo(3);
        assertThat(value.volumeSetCount()).isEqualTo(2);
    }

    @Test
    void shouldDistinguishMissingVolumeFromRecordedZero() {
        assertThat(calculateStrength(List.of()).maxNormalizedLoadKg()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(null, null, 10))).volumeKgReps()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(BigDecimal.ZERO, TOTAL, null))).volumeKgReps()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(new BigDecimal("50"), TOTAL, null))).maxNormalizedLoadKg()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(BigDecimal.ZERO, TOTAL, null))).maxNormalizedLoadKg()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(BigDecimal.ONE, null, 1))).maxNormalizedLoadKg()).isNull();
        assertThat(calculateStrength(List.of(new SetExecution(BigDecimal.ONE, TOTAL, 0))).maxNormalizedLoadKg()).isNull();
        var zero = calculateStrength(List.of(new SetExecution(BigDecimal.ZERO, PER_SIDE, 10)));
        assertThat(zero.maxNormalizedLoadKg()).isEqualByComparingTo("0");
        assertThat(zero.volumeKgReps()).isEqualByComparingTo("0");
        var notesOnly = calculateStrength(List.of(new SetExecution(null, null, null)));
        assertThat(notesOnly.registeredSetCount()).isOne();
        assertThat(notesOnly.maxNormalizedLoadKg()).isNull();
        assertThat(notesOnly.volumeKgReps()).isNull();
    }

    @Test
    void shouldSumAllSetsAndAvoidIntegerOrPersistencePrecisionLimits() {
        var set = new SetExecution(new BigDecimal("999999.99"), PER_SIDE, Integer.MAX_VALUE);
        var value = calculateStrength(List.of(set, set));
        assertThat(value.maxNormalizedLoadKg()).isEqualByComparingTo("1999999.98");
        assertThat(value.volumeKgReps()).isEqualByComparingTo(
                new BigDecimal("3999999.96").multiply(BigDecimal.valueOf(Integer.MAX_VALUE)));
        assertThat(calculateStrength(List.of(new SetExecution(new BigDecimal("20"), TOTAL, 10),
                new SetExecution(new BigDecimal("20"), PER_SIDE, 10))).volumeKgReps())
                .isEqualByComparingTo("600");
    }

    @Test
    void shouldPreserveIndependentMeasurementsAndNeverPairSeparateActivities() {
        var value = calculateCardio(List.of(new ActivityExecution(20, null),
                new ActivityExecution(null, new BigDecimal("5")), new ActivityExecution(null, null)));
        assertThat(value.durationMinutes()).isEqualTo(20L);
        assertThat(value.distanceKm()).isEqualByComparingTo("5");
        assertThat(value.averagePaceMinPerKm()).isNull();
        assertThat(value.averageSpeedKmPerHour()).isNull();
        assertThat(value.activityCount()).isEqualTo(3);
        assertThat(value.durationRecordCount()).isOne();
        assertThat(value.distanceRecordCount()).isOne();
        assertThat(calculateCardio(List.of()).durationMinutes()).isNull();
        assertThat(calculateCardio(List.of(new ActivityExecution(null, null))).distanceKm()).isNull();
    }

    @Test
    void shouldUseEligibleTotalsRatherThanArithmeticMeansOrGeneralTotals() {
        var value = calculateCardio(List.of(new ActivityExecution(30, new BigDecimal("5")),
                new ActivityExecution(10, BigDecimal.ONE), new ActivityExecution(100, null),
                new ActivityExecution(null, new BigDecimal("100"))));
        assertThat(value.averagePaceMinPerKm()).isEqualByComparingTo("6.67");
        assertThat(value.averageSpeedKmPerHour()).isEqualByComparingTo("9.00");
        assertThat(value.durationMinutes()).isEqualTo(140);
        assertThat(value.distanceKm()).isEqualByComparingTo("106");
        assertThat(value.paceRecordCount()).isEqualTo(2);
        assertThat(value.speedRecordCount()).isEqualTo(2);
    }

    @Test
    void shouldUseSeparatePaceAndSpeedEligibilityAndProtectZeroDenominators() {
        var zero = calculateCardio(List.of(new ActivityExecution(30, BigDecimal.ZERO)));
        assertThat(zero.averagePaceMinPerKm()).isNull();
        assertThat(zero.averageSpeedKmPerHour()).isEqualByComparingTo("0");
        assertThat(zero.paceRecordCount()).isZero();
        assertThat(zero.speedRecordCount()).isOne();
        var mixed = calculateCardio(List.of(new ActivityExecution(30, BigDecimal.ZERO),
                new ActivityExecution(30, new BigDecimal("5"))));
        assertThat(mixed.averagePaceMinPerKm()).isEqualByComparingTo("6");
        assertThat(mixed.averageSpeedKmPerHour()).isEqualByComparingTo("5");
        var invalid = calculateCardio(List.of(new ActivityExecution(0, BigDecimal.ZERO)));
        assertThat(invalid.averageSpeedKmPerHour()).isNull();
        assertThat(invalid.averagePaceMinPerKm()).isNull();
    }

    @Test
    void shouldRoundOnlyFinalRatiosAndRespectSmallValuesAndLargeDurationTotals() {
        var value = calculateCardio(List.of(new ActivityExecution(1, new BigDecimal("0.01"))));
        assertThat(value.averagePaceMinPerKm()).isEqualByComparingTo("100");
        assertThat(value.averageSpeedKmPerHour()).isEqualByComparingTo("0.60");
        var rounded = calculateCardio(List.of(new ActivityExecution(1, new BigDecimal("32"))));
        assertThat(rounded.averagePaceMinPerKm()).isEqualByComparingTo("0.03");
        var huge = calculateCardio(List.of(new ActivityExecution(Integer.MAX_VALUE, BigDecimal.ONE),
                new ActivityExecution(Integer.MAX_VALUE, BigDecimal.ONE)));
        assertThat(huge.durationMinutes()).isEqualTo(4294967294L);
        assertThat(huge.averageSpeedKmPerHour()).isEqualByComparingTo("0.00");
        var half = calculateCardio(List.of(new ActivityExecution(1, new BigDecimal("8"))));
        assertThat(half.averagePaceMinPerKm()).isEqualByComparingTo("0.13");
    }
}
