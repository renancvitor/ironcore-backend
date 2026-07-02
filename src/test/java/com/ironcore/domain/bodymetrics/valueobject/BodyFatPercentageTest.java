package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyFatPercentageTest {

    @Test
    void shouldReturnExpectedBodyFatPercentage() {
        BodyFatPercentage validPercentage = new BodyFatPercentage(10.3);

        assertThat(validPercentage.value()).isEqualTo(10.3);
    }

    @Test
    void shouldAcceptZeroBodyFatPercentage() {
        BodyFatPercentage bodyFatPercentage = new BodyFatPercentage(0.0);

        assertThat(bodyFatPercentage.value()).isZero();
    }

    @Test
    void shouldFailWhenBodyFatPercentageIsNull() {
        assertThatThrownBy(() -> new BodyFatPercentage(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyFatPercentageIsNotFinite() {
        assertThatThrownBy(() -> new BodyFatPercentage(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyFatPercentageIsNegative() {
        assertThatThrownBy(() -> new BodyFatPercentage(-1.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyFatPercentageIsGreaterThanMaximumAllowed() {
        assertThatThrownBy(() -> new BodyFatPercentage(100.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

}
