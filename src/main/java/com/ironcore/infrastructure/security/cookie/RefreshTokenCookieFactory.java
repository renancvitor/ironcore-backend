package com.ironcore.infrastructure.security.cookie;

import com.ironcore.application.auth.port.SessionCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
public class RefreshTokenCookieFactory {

    private final Clock clock;
    private final String sameSite;

    public RefreshTokenCookieFactory(Clock clock, @Value("${security.cookie.same-site:None}") String sameSite) {
        this.clock = clock;
        this.sameSite = sameSite;
    }

    public String create(SessionCredentials credentials) {
        Duration maxAge = Duration.between(clock.instant(), credentials.sessionExpiresAt());
        return cookie(credentials.refreshToken(), maxAge.isNegative() ? Duration.ZERO : maxAge);
    }

    public String clear() {
        return cookie("", Duration.ZERO);
    }

    private String cookie(String value, Duration maxAge) {
        return ResponseCookie.from("refresh_token", value)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build()
                .toString();
    }
}
