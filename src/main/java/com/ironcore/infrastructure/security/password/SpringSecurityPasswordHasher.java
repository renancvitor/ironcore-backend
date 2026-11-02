package com.ironcore.infrastructure.security.password;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;
    private final String fallbackHash;

    public SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.fallbackHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        return new PasswordHash(passwordEncoder.encode(rawPassword.value()));
    }

    @Override
    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        boolean matches = passwordEncoder.matches(rawPassword.value(), passwordHash == null ? fallbackHash : passwordHash.value());
        return passwordHash != null && matches;
    }
}
