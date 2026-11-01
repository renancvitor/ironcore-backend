package com.ironcore.application.user.service;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHashingServiceTest {

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private PasswordHashingService passwordHashingService;

    @Nested
    class Hash {

        @ParameterizedTest
        @ValueSource(strings = {"ascii", "unicode"})
        void shouldRejectNewPasswordsExceeding72Utf8BytesBeforeHashing(String kind) {
            var password = new RawPassword(kind.equals("ascii") ? "a".repeat(73) : "á".repeat(37));
            assertThatThrownBy(() -> passwordHashingService.hash(password))
                    .isInstanceOf(com.ironcore.domain.user.exception.InvalidPasswordException.class);
            verifyNoInteractions(passwordHasher);
        }

        @Test
        void shouldAcceptExactly72Utf8BytesForNewPasswords() {
            var password = new RawPassword("á".repeat(36));
            var hash = new PasswordHash("hash");
            when(passwordHasher.hash(password)).thenReturn(hash);
            assertThat(passwordHashingService.hash(password)).isEqualTo(hash);
        }

        @Test
        void shouldHashRawPassword() {
            RawPassword rawPassword = new RawPassword("StrongPass123");

            PasswordHash passwordHash = new PasswordHash("hashed-password");

            when(passwordHasher.hash(rawPassword))
                    .thenReturn(passwordHash);

            PasswordHash result = passwordHashingService.hash(rawPassword);

            verify(passwordHasher).hash(rawPassword);

            assertThat(result).isEqualTo(passwordHash);
        }
    }

    @Nested
    class Matches {

        @Test
        void shouldDelegateLegacyPasswordsWithoutTruncationOrLengthValidation() {
            var password = new RawPassword("a".repeat(100));
            var hash = new PasswordHash("legacy-hash");
            when(passwordHasher.matches(password, hash)).thenReturn(true);
            assertThat(passwordHashingService.matches(password, hash)).isTrue();
            verify(passwordHasher).matches(password, hash);
        }

        @Test
        void shouldMatchRawPasswordAndPasswordHash() {
            RawPassword rawPassword = new RawPassword("StrongPass123");

            PasswordHash passwordHash = new PasswordHash("hashed-password");

            when(passwordHasher.matches(rawPassword, passwordHash))
                    .thenReturn(true);

            boolean result = passwordHashingService.matches(rawPassword, passwordHash);

            verify(passwordHasher).matches(rawPassword, passwordHash);

            assertThat(result)
                    .isTrue();
        }
    }
}
