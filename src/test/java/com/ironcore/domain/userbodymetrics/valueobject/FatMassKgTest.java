package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FatMassKgTest {

    private static final double MAX_EXPECTED_WEIGHT_KG = 500.0;

    @Test
    void shouldReturnExpectedFatMassKg() {
        FatMassKg correctlyWeight = new FatMassKg(70.0);

        assertThat(correctlyWeight.value()).isLessThan(MAX_EXPECTED_WEIGHT_KG);
    }

    @Test
    void shouldFailWhenFatMassKgIsNull() {
        assertThatThrownBy(() -> new FatMassKg(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenFatMassKgIsNotFinite() {
        assertThatThrownBy(() -> new FatMassKg(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenFatMassKgIsNotPositive() {
        assertThatThrownBy(() -> new FatMassKg(-100.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenFatMassKgIsMajorThanMaxExpectedWeightKg() {
        assertThatThrownBy(() -> new FatMassKg(500.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
