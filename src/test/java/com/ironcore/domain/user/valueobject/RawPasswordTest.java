package com.ironcore.domain.user.valueobject;

import com.ironcore.domain.user.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTest {

    @Test
    void shouldAcceptLegacyPasswordsAndRedactDiagnosticOutput() {
        assertThat(new RawPassword("a".repeat(72)).value()).hasSize(72);
        assertThat(new RawPassword("a".repeat(73)).value()).hasSize(73);
        assertThat(new RawPassword("á".repeat(37)).value()).hasSize(37);
        assertThat(new RawPassword("Secret123@").toString()).doesNotContain("Secret123@");
    }

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
