package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void shouldCreateEmailWhenValueIsValid() {
        Email email = new Email(" RENAN@example.com ");

        assertThat(email.value()).isEqualTo("renan@example.com");
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void shouldRejectWhenEmailIsBlank() {
        assertThatThrownBy(() -> new Email(" "))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        assertThatThrownBy(() -> new Email("renan.com"))
                .isInstanceOf(InvalidEmailException.class);
    }
}
