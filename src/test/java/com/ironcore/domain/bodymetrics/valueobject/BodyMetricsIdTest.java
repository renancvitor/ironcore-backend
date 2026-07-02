package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidPersonBodyMetricsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyMetricsIdTest {

    @Test
    void shouldCreateUserBodyMetricsIdWhenValueIsPositive() {
        BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);

        assertThat(bodyMetricsId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsNull() {
        assertThatThrownBy(() -> new BodyMetricsId(null))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsZero() {
        assertThatThrownBy(() -> new BodyMetricsId(0L))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsNegative() {
        assertThatThrownBy(() -> new BodyMetricsId(-1L))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }
}
