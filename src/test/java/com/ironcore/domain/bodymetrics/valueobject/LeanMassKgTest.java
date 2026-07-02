package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeanMassKgTest {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    @Test
    void shouldReturnExpectedLeanMassKg() {
        LeanMassKg correctlyWeight = new LeanMassKg(70.0);

        assertThat(correctlyWeight.value()).isLessThan(MAX_EXPECTED_WEIGHT_KG);
    }

    @Test
    void shouldFailWhenLeanMassKgIsNull() {
        assertThatThrownBy(() -> new LeanMassKg(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenLeanMassKgIsNotFinite() {
        assertThatThrownBy(() -> new LeanMassKg(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenLeanMassKgIsNotPositive() {
        assertThatThrownBy(() -> new LeanMassKg(-100.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenLeanMassKgIsMajorThanMaxExpectedWeightKg() {
        assertThatThrownBy(() -> new LeanMassKg(500.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
