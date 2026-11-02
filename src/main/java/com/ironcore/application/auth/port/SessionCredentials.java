package com.ironcore.application.auth.port;

import java.time.Instant;

public record SessionCredentials(
        GeneratedAccessToken accessToken,
        String refreshToken,
        Instant sessionExpiresAt
) {
    @Override
    public String toString() {
        return "SessionCredentials[accessToken=[REDACTED], refreshToken=[REDACTED], sessionExpiresAt="
                + sessionExpiresAt + "]";
    }
}
