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
