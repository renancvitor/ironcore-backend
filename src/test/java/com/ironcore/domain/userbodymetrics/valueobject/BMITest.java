package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BMITest {

    @Test
    void shouldReturnExpectedBMI() {
        BMI bmi = new BMI(24.5);

        assertThat(bmi.value()).isEqualTo(24.5);
    }

    @Test
    void shouldAcceptZeroBMI() {
        BMI bmi = new BMI(0.0);

        assertThat(bmi.value()).isZero();
    }

    @Test
    void shouldFailWhenBMIIsNull() {
        assertThatThrownBy(() -> new BMI(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBMIIsNotFinite() {
        assertThatThrownBy(() -> new BMI(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBMIIsNegative() {
        assertThatThrownBy(() -> new BMI(-1.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBMIIsGreaterThanMaximumAllowed() {
        assertThatThrownBy(() -> new BMI(300.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
