package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordHashTest {

    @Test
    void shouldAcceptPasswordHashWhenValueIsValid() {
        PasswordHash passwordHash = new PasswordHash("hash-password");

        assertThat(passwordHash.value()).isEqualTo("hash-password");
    }

    @Test
    void shouldFailWhenPasswordHashIsNull() {
        assertThatThrownBy(() -> new PasswordHash(null))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void shouldFailWhenPasswordHashIsBlank() {
        assertThatThrownBy(() -> new PasswordHash(""))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
