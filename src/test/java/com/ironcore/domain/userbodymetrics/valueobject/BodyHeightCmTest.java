package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyHeightCmTest {

    @Test
    void shouldReturnExpectedBodyHeightCm() {
        BodyHeightCm bodyHeightCm = new BodyHeightCm(180.0);

        assertThat(bodyHeightCm.value()).isEqualTo(180.0);
    }

    @Test
    void shouldReturnBodyHeightInMeters() {
        BodyHeightCm bodyHeightCm = new BodyHeightCm(180.0);

        assertThat(bodyHeightCm.inMeters()).isEqualTo(1.8);
    }

    @Test
    void shouldFailWhenBodyHeightCmIsNull() {
        assertThatThrownBy(() -> new BodyHeightCm(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyHeightCmIsNotFinite() {
        assertThatThrownBy(() -> new BodyHeightCm(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyHeightCmIsZero() {
        assertThatThrownBy(() -> new BodyHeightCm(0.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyHeightCmIsNegative() {
        assertThatThrownBy(() -> new BodyHeightCm(-1.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyHeightCmIsGreaterThanMaximumAllowed() {
        assertThatThrownBy(() -> new BodyHeightCm(300.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
