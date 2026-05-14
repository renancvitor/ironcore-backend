package com.ironcore.infrastructure.security.password;

import com.ironcore.domain.user.port.PasswordHasher;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        return new PasswordHash(passwordEncoder.encode(rawPassword.value()));
    }

    @Override
    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        return passwordEncoder.matches(rawPassword.value(), passwordHash.value());
    }
}
