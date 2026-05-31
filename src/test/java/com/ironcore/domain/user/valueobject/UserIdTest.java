package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void shouldCreateUserIdWhenValueIsPositive() {
        UserId userId = new UserId(1L);

        assertThat(userId.value()).isEqualTo(1L);
    }

    @Test
    void shouldFailWhenUserIdIsNull() {
        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void shouldFailWhenUserIdIsZero() {
        assertThatThrownBy(() -> new UserId(0L))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void shouldFailWhenUserIdIsNegative() {
        assertThatThrownBy(() -> new UserId(-1L))
                .isInstanceOf(InvalidUserException.class);
    }
}
