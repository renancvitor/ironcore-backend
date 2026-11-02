package com.ironcore.infrastructure.security.password;

import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SpringSecurityPasswordHasherTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SpringSecurityPasswordHasher passwordHasher = new SpringSecurityPasswordHasher(passwordEncoder);

    @Test
    void rejectsNewPasswordsBeyondBcryptByteLimitWithoutTruncation() {
        for (String password : java.util.List.of("a".repeat(73), "\u00e1".repeat(37))) {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> passwordHasher.hash(new RawPassword(password)))
                    .isInstanceOf(com.ironcore.domain.user.exception.InvalidPasswordException.class);
        }
        var maximum = new RawPassword("\u00e1".repeat(36));
        assertThat(passwordHasher.matches(maximum, passwordHasher.hash(maximum))).isTrue();
    }

    @Test
    void rejectsOversizedLoginPasswordsForExistingAndMissingAccounts() {
        var valid = passwordHasher.hash(new RawPassword("ValidPassword123@"));
        var oversized = new RawPassword("a".repeat(73));
        assertThat(passwordHasher.matches(oversized, valid)).isFalse();
        assertThat(passwordHasher.matches(oversized, null)).isFalse();
    }

    @Nested
    class Hash {

        @Test
        void shouldGeneratePasswordHashDifferentFromRawPassword() {
            RawPassword rawPassword = new RawPassword("StrongPassword123");

            PasswordHash passwordHash = passwordHasher.hash(rawPassword);

            assertThat(passwordHash.value())
                    .isNotBlank()
                    .isNotEqualTo(rawPassword.value());
        }
    }

    @Nested
    class Matches {

        @Test
        void shouldVerifyAgainstAFallbackHashWhenTheAccountDoesNotExist() {
            PasswordEncoder encoder = org.mockito.Mockito.spy(new BCryptPasswordEncoder());
            var hasher = new SpringSecurityPasswordHasher(encoder);
            var password = new RawPassword("UnknownAccountPassword");

            assertThat(hasher.matches(password, null)).isFalse();
            org.mockito.Mockito.verify(encoder).matches(org.mockito.ArgumentMatchers.eq(password.value()),
                    org.mockito.ArgumentMatchers.startsWith("$2"));
        }

        @Test
        void shouldReturnTrueWhenRawPasswordMatchesHash() {
            RawPassword rawPassword = new RawPassword("StrongPassword123");

            PasswordHash passwordHash = passwordHasher.hash(rawPassword);

            boolean matches = passwordHasher.matches(rawPassword, passwordHash);

            assertThat(matches)
                    .isTrue();
        }

        @Test
        void shouldReturnFalseWhenRawPasswordDoesNotMatchHash() {
            RawPassword rawPassword = new RawPassword("StrongPassword123");

            PasswordHash passwordHash = passwordHasher.hash(rawPassword);

            RawPassword wrongPassword = new RawPassword("WrongPassword123");

            boolean matches = passwordHasher.matches(wrongPassword, passwordHash);

            assertThat(matches)
                    .isFalse();
        }
    }
}
