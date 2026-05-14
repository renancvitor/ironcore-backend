package com.ironcore.application.user.service;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHashingServiceTest {

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private PasswordHashingService passwordHashingService;

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
