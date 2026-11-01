package com.ironcore.infrastructure.security.token;

import com.ironcore.application.auth.port.RefreshTokenGenerator;
import com.ironcore.application.exception.InvalidCredentialsException;
import com.ironcore.domain.auth.valueobject.AuthSessionId;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class SecureRefreshTokenGenerator implements RefreshTokenGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate(AuthSessionId sessionId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return sessionId.value() + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }

    @Override
    public AuthSessionId sessionId(String token) {
        if (token == null || !token.matches("[0-9a-fA-F-]{36}\\.[A-Za-z0-9_-]{43}")) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }
        try {
            return new AuthSessionId(UUID.fromString(token.substring(0, 36)));
        } catch (IllegalArgumentException exception) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }
    }
}
