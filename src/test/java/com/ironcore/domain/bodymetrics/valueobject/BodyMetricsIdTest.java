package com.ironcore.domain.bodymetrics.valueobject;

import com.ironcore.domain.bodymetrics.exception.InvalidPersonBodyMetricsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyMetricsIdTest {

    @Test
    void shouldCreatePersonBodyMetricsIdWhenValueIsPositive() {
        BodyMetricsId bodyMetricsId = new BodyMetricsId(1L);

        assertThat(bodyMetricsId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenPersonBodyMetricsIdIsNull() {
        assertThatThrownBy(() -> new BodyMetricsId(null))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenPersonBodyMetricsIdIsZero() {
        assertThatThrownBy(() -> new BodyMetricsId(0L))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenPersonBodyMetricsIdIsNegative() {
        assertThatThrownBy(() -> new BodyMetricsId(-1L))
                .isInstanceOf(InvalidPersonBodyMetricsException.class);
    }
}
