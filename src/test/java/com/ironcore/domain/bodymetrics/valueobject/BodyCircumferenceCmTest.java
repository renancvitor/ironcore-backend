package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidBodyMetricException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyCircumferenceCmTest {

    @Test
    void shouldReturnExpectedBodyCircumferenceCm() {
        BodyCircumferenceCm bodyCircumferenceCm = new BodyCircumferenceCm(90.0);

        assertThat(bodyCircumferenceCm.value()).isEqualTo(90.0);
    }

    @Test
    void shouldFailWhenBodyCircumferenceCmIsNull() {
        assertThatThrownBy(() -> new BodyCircumferenceCm(null))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyCircumferenceCmIsNotFinite() {
        assertThatThrownBy(() -> new BodyCircumferenceCm(Double.POSITIVE_INFINITY))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyCircumferenceCmIsZero() {
        assertThatThrownBy(() -> new BodyCircumferenceCm(0.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyCircumferenceCmIsNegative() {
        assertThatThrownBy(() -> new BodyCircumferenceCm(-1.0))
                .isInstanceOf(InvalidBodyMetricException.class);
    }

    @Test
    void shouldFailWhenBodyCircumferenceCmIsGreaterThanMaximumAllowed() {
        assertThatThrownBy(() -> new BodyCircumferenceCm(300.1))
                .isInstanceOf(InvalidBodyMetricException.class);
    }
}
