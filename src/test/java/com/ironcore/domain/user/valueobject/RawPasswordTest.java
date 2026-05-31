package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTest {

    @Test
    void shouldAcceptPasswordWhenEightCharactersOrMore() {
        RawPassword rawPassword = new RawPassword("12345AbCd0");

        assertThat(rawPassword.value()).isEqualTo("12345AbCd0");
    }

    @Test
    void shouldFailWhenNullPassword() {
        assertThatThrownBy(() -> new RawPassword(null))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void shouldFailWhenEmptyPassword() {
        assertThatThrownBy(() -> new RawPassword(""))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void shouldFailWhenPasswordIsLowerThanEightCharacters() {
        assertThatThrownBy(() -> new RawPassword("12345"))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
