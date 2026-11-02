package com.ironcore.application.auth.port;

import java.time.LocalDateTime;

public record GeneratedAccessToken(
        String value,
        String tokenType,
        LocalDateTime expiresAt
) {
    @Override
    public String toString() {
        return "GeneratedAccessToken[value=[REDACTED], tokenType=" + tokenType + ", expiresAt=" + expiresAt + "]";
    }
}
