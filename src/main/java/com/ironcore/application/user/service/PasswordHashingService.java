package com.ironcore.application.user.service;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordHashingService {

    private final PasswordHasher passwordHasher;

    public PasswordHash hash(RawPassword rawPassword) {
        return passwordHasher.hash(rawPassword);
    }

    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        return passwordHasher.matches(rawPassword, passwordHash);
    }
}
