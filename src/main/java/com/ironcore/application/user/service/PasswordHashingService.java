package com.ironcore.application.user.service;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.exception.InvalidPasswordException;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordHashingService {

    // Public work-factor placeholder, never an accepted account credential.
    private static final PasswordHash DUMMY_HASH = new PasswordHash(
            "$2a$10$....................................................."
    );

    private final PasswordHasher passwordHasher;

    public PasswordHash hash(RawPassword rawPassword) {
        if (rawPassword.value().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordException("A nova senha deve ter no máximo 72 bytes em UTF-8.");
        }
        return passwordHasher.hash(rawPassword);
    }

    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        return passwordHasher.matches(rawPassword, passwordHash);
    }

    public void verifyAgainstDummyHash(RawPassword rawPassword) {
        passwordHasher.matches(rawPassword, DUMMY_HASH);
    }
}
