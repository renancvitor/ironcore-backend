package com.ironcore.application.auth.port;

import java.time.Instant;

import java.time.LocalDateTime;

public record GeneratedAccessToken(
        String value,
        String tokenType,
        LocalDateTime expiresAt,
        String refreshToken,
        Instant refreshExpiresAt
) {
    public GeneratedAccessToken(String value, String tokenType, LocalDateTime expiresAt) {
        this(value, tokenType, expiresAt, null, null);
    }

    @Override
    public String toString() { return "GeneratedAccessToken[redacted]"; }
}
