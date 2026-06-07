package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyWeightKgTest {

    @Test
    void shouldReturnExpectedBodyWeightKg() {
        BodyWeightKg bodyWeightKg = new BodyWeightKg(80.0);

        assertThat(bodyWeightKg.value()).isEqualTo(80.0);
    }

    @Test
    void shouldFailWhenBodyWeightKgIsNull() {
        assertThatThrownBy(() -> new BodyWeightKg(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyWeightKgIsNotFinite() {
        assertThatThrownBy(() -> new BodyWeightKg(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyWeightKgIsZero() {
        assertThatThrownBy(() -> new BodyWeightKg(0.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyWeightKgIsNegative() {
        assertThatThrownBy(() -> new BodyWeightKg(-1.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyWeightKgIsGreaterThanMaximumAllowed() {
        assertThatThrownBy(() -> new BodyWeightKg(500.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
