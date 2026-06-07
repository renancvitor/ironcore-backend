package com.ironcore.domain.userbodymetrics.valueobject;

import com.ironcore.domain.userbodymetrics.exception.InvalidUserBodyMetricsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserBodyMetricsIdTest {

    @Test
    void shouldCreateUserBodyMetricsIdWhenValueIsPositive() {
        UserBodyMetricsId userBodyMetricsId = new UserBodyMetricsId(1L);

        assertThat(userBodyMetricsId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsNull() {
        assertThatThrownBy(() -> new UserBodyMetricsId(null))
                .isInstanceOf(InvalidUserBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsZero() {
        assertThatThrownBy(() -> new UserBodyMetricsId(0L))
                .isInstanceOf(InvalidUserBodyMetricsException.class);
    }

    @Test
    void shouldFailWhenUserBodyMetricsIdIsNegative() {
        assertThatThrownBy(() -> new UserBodyMetricsId(-1L))
                .isInstanceOf(InvalidUserBodyMetricsException.class);
    }
}
